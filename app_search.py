"""
Interface web atualizada para busca universal de livros
"""

# pylint: disable=broad-exception-caught

import logging
import os
import threading
import webbrowser
import time
from typing import Dict
from flask import Flask, render_template, request, jsonify, send_file
from flask_cors import CORS
from book_search import BookSearcher

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__, template_folder="frontend", static_folder="frontend/static")
CORS(app)

# Configurações
DEFAULT_DOWNLOAD_DIR = os.environ.get("DOWNLOADS_DIR", r"G:\Livros_Archive")
os.makedirs(DEFAULT_DOWNLOAD_DIR, exist_ok=True)

searcher = BookSearcher()
search_results = {}
download_status = {}


@app.route("/")
def index():
    """Serve página principal"""
    return render_template("search.html")


@app.route("/api/search", methods=["POST"])
def search_books():
    """Busca livros"""
    try:
        data = request.get_json()
        title = data.get("title", "")
        author = data.get("author", "")

        if not title:
            return jsonify({"error": "Título é obrigatório"}), 400

        logger.info("Buscando: %s - %s", title, author)
        results = searcher.search(title, author)

        search_id = f"{title}_{author}_{len(search_results)}"
        search_results[search_id] = results

        return jsonify(
            {
                "search_id": search_id,
                "count": len(results),
                "results": results[:50],  # Limitar a 50 resultados
            }
        )
    except Exception as e:
        logger.error("Erro na busca: %s", e)
        return jsonify({"error": str(e)}), 500


@app.route("/api/download", methods=["POST"])
def start_download():
    """Inicia download de um livro"""
    try:
        data = request.get_json()
        book_index = data.get("book_index")
        search_id = data.get("search_id")

        if search_id not in search_results:
            return jsonify({"error": "Busca não encontrada"}), 404

        if book_index >= len(search_results[search_id]):
            return jsonify({"error": "Livro não encontrado"}), 404

        book = search_results[search_id][book_index]

        # Criar nome de arquivo seguro e CURTO
        # Usar apenas: Autor - Título (max 100 caracteres)
        author = book.get("author", "Unknown")[:30]  # Limitar autor a 30 chars
        title = book.get("title", "Unknown")[:50]  # Limitar título a 50 chars

        # Limpar caracteres inválidos para Windows
        filename = f"{author} - {title}.{book['format'].lower()}"
        filename = "".join(
            c for c in filename if c.isalnum() or c in (" ", "-", "_", ".")
        ).strip()

        # Sempre usar pasta padrão
        output_path = os.path.join(DEFAULT_DOWNLOAD_DIR, filename)
        download_id = f"download_{len(download_status)}"

        download_status[download_id] = {
            "status": "iniciando",
            "progress": 0,
            "message": "Iniciando download...",
            "filename": filename,
            "url": book.get("url"),
        }

        # Download em thread separada
        thread = threading.Thread(
            target=_download_thread, args=(download_id, book, output_path)
        )
        thread.daemon = True
        thread.start()

        return jsonify({"download_id": download_id, "filename": filename})

    except Exception as e:
        logger.error("Erro ao iniciar download: %s", e)
        return jsonify({"error": str(e)}), 500


def _download_thread(download_id: str, book: Dict, output_path: str):
    """Thread para download"""
    try:
        download_status[download_id]["status"] = "baixando"
        download_status[download_id]["message"] = f"Baixando de {book['source']}..."

        logger.info("Iniciando download: %s", book.get("title"))
        logger.info("URL: %s", book.get("url"))
        logger.info("Salvando em: %s", output_path)

        success = searcher.download_book(book, output_path)

        if success:
            download_status[download_id] = {
                "status": "completo",
                "progress": 100,
                "message": "Download concluído!",
                "file_path": output_path,
                "pdf_url": f"/api/download-pdf/{os.path.basename(output_path)}",
            }
            logger.info("✓ Download sucesso: %s", output_path)
        else:
            logger.error("❌ Download falhou para: %s", book.get("title"))
            download_status[download_id] = {
                "status": "erro",
                "message": "Falha no download - URL inválida ou arquivo não acessível",
            }
    except Exception as e:
        logger.error("Erro fatal no download thread: %s", e, exc_info=True)
        download_status[download_id] = {"status": "erro", "message": f"Erro: {str(e)}"}


@app.route("/api/download-status/<download_id>")
def get_download_status(download_id: str):
    """Retorna status do download"""
    if download_id in download_status:
        return jsonify(download_status[download_id])
    return jsonify({"error": "Download não encontrado"}), 404


@app.route("/api/download-pdf/<filename>")
def download_pdf(filename: str):
    """Serve o arquivo PDF para download direto pelo navegador"""
    try:
        file_path = os.path.join(DEFAULT_DOWNLOAD_DIR, filename)

        # Validar caminho para evitar directory traversal
        if not os.path.abspath(file_path).startswith(
            os.path.abspath(DEFAULT_DOWNLOAD_DIR)
        ):
            return jsonify({"error": "Acesso negado"}), 403

        if os.path.exists(file_path):
            return send_file(file_path, as_attachment=True, download_name=filename)

        return jsonify({"error": "Arquivo não encontrado"}), 404
    except Exception as e:
        logger.error("Erro ao servir arquivo %s: %s", filename, e)
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    print("=" * 80)
    print("meu_Busca_Livros")
    print("=" * 80)
    print(f"Pasta de downloads: {DEFAULT_DOWNLOAD_DIR}")
    print("Acesse: http://127.0.0.1:5000")
    print("=" * 80)

    # Abrir navegador automaticamente após 1.5 segundos
    def open_browser():
        time.sleep(1.5)  # Aguardar servidor inicializar
        try:
            webbrowser.open("http://127.0.0.1:5000")
            print("\n✓ Navegador aberto automaticamente!")
        except Exception as e:
            print(f"\n⚠ Não foi possível abrir o navegador: {e}")
            print("Por favor, abra manualmente: http://127.0.0.1:5000")

    threading.Thread(target=open_browser, daemon=True).start()

    app.run(debug=False, port=5000)
