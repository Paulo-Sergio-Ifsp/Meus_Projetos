"""
meu_Busca_Livros - Executável Standalone
Wrapper para executável que gerencia abertura de navegador e recursos
"""

import sys
import os
import webbrowser
import threading
import time
import atexit
from pathlib import Path

# Definir diretório base (funciona tanto em dev quanto em .exe)
if getattr(sys, "frozen", False):
    # Rodando como executável
    base_dir_str = getattr(sys, "_MEIPASS", None)
    if not base_dir_str:
        base_dir_str = str(Path(__file__).parent)
    BASE_DIR = Path(base_dir_str)
    TEMPLATE_FOLDER = BASE_DIR / "frontend"
    STATIC_FOLDER = BASE_DIR / "frontend" / "static"
else:
    # Rodando como script
    BASE_DIR = Path(__file__).parent
    TEMPLATE_FOLDER = BASE_DIR / "frontend"
    STATIC_FOLDER = BASE_DIR / "frontend" / "static"

# Importar após definir paths
import logging
from typing import Dict
from flask import Flask, render_template, request, jsonify, send_file
from flask_cors import CORS
from book_search import BookSearcher

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(
    __name__, template_folder=str(TEMPLATE_FOLDER), static_folder=str(STATIC_FOLDER)
)
CORS(app)

# Configurações
DEFAULT_DOWNLOAD_DIR = os.environ.get("DOWNLOADS_DIR", r"G:\Livros_Archive")
os.makedirs(DEFAULT_DOWNLOAD_DIR, exist_ok=True)

searcher = BookSearcher()
search_results = {}
download_status = {}


@app.route("/")
def index():
    """Página inicial"""
    return render_template("search.html")


@app.route("/api/search", methods=["POST"])
def search():
    """Endpoint de busca"""
    data = request.get_json(silent=True) or {}
    title = data.get("title", "")
    author = data.get("author", "")

    if not title:
        return jsonify({"error": "Título é obrigatório"}), 400

    logger.info("Buscando: %s - %s", title, author)

    results = searcher.search(title, author)

    search_id = f"search_{len(search_results)}"
    search_results[search_id] = results

    return jsonify({"results": results, "search_id": search_id})


@app.route("/api/download", methods=["POST"])
def start_download():
    """Inicia download de um livro"""
    data = request.get_json(silent=True) or {}
    book_index = data.get("book_index")
    search_id = data.get("search_id")

    if search_id not in search_results:
        return jsonify({"error": "Resultados de busca não encontrados"}), 404

    results = search_results[search_id]
    if book_index >= len(results):
        return jsonify({"error": "Índice de livro inválido"}), 400

    book = results[book_index]

    download_id = f"download_{len(download_status)}"
    download_status[download_id] = {"status": "starting", "progress": 0}

    logger.info("Iniciando download: %s", book["title"])

    def _download_thread():
        try:
            author = book.get("author", "Unknown")
            title = book.get("title", "Unknown")
            book_format = book.get("format", "pdf")

            author = author[:30] if author else "Unknown"
            title = title[:50] if title else "Unknown"

            filename = f"{author} - {title}.{book_format.lower()}"

            safe_filename = "".join(
                c for c in filename if c.isalnum() or c in (" ", "-", "_", ".")
            ).strip()

            output_path = os.path.join(DEFAULT_DOWNLOAD_DIR, safe_filename)

            logger.info("URL: %s", book.get("url"))
            logger.info("Salvando em: %s", output_path)

            download_status[download_id] = {
                "status": "downloading",
                "progress": 50,
                "filename": safe_filename,
            }

            searcher.download_book(book, output_path)

            download_status[download_id] = {
                "status": "completed",
                "progress": 100,
                "filename": safe_filename,
                "path": output_path,
            }
            logger.info("✓ Download sucesso: %s", output_path)

        except Exception as e:
            logger.error("❌ Download falhou para: %s", book["title"])
            logger.exception(e)
            download_status[download_id] = {"status": "error", "error": str(e)}

    threading.Thread(target=_download_thread, daemon=True).start()

    return jsonify({"download_id": download_id})


@app.route("/api/download-status/<download_id>")
def get_download_status(download_id):
    """Retorna status de um download"""
    if download_id not in download_status:
        return jsonify({"error": "Download não encontrado"}), 404

    return jsonify(download_status[download_id])


@app.route("/api/download-pdf/<filename>")
def download_pdf(filename):
    """Serve arquivo para download"""
    try:
        file_path = os.path.join(DEFAULT_DOWNLOAD_DIR, filename)

        if not os.path.exists(file_path):
            logger.error("Arquivo não existe: %s", file_path)
            return jsonify({"error": "Arquivo não encontrado"}), 404

        if not file_path.startswith(os.path.abspath(DEFAULT_DOWNLOAD_DIR)):
            logger.error("Tentativa de acesso a arquivo fora do diretório permitido")
            return jsonify({"error": "Acesso negado"}), 403

        if os.path.exists(file_path):
            return send_file(file_path, as_attachment=True, download_name=filename)

        return jsonify({"error": "Arquivo não encontrado"}), 404
    except Exception as e:
        logger.error("Erro ao servir arquivo %s: %s", filename, e)
        return jsonify({"error": str(e)}), 500


def open_browser():
    """Abre o navegador após delay"""
    time.sleep(2)
    try:
        webbrowser.open("http://127.0.0.1:5000")
        logger.info("\n✓ Navegador aberto automaticamente!")
    except Exception as e:
        logger.warning("\n⚠ Não foi possível abrir o navegador: %s", e)
        print("\nPor favor, abra manualmente: http://127.0.0.1:5000")


def cleanup():
    """Limpeza ao encerrar"""
    logger.info("Encerrando servidor...")


if __name__ == "__main__":
    atexit.register(cleanup)

    print("=" * 80)
    print("meu_Busca_Livros v2.0")
    print("by Activaware")
    print("=" * 80)
    print(f"Pasta de downloads: {DEFAULT_DOWNLOAD_DIR}")
    print("Acesse: http://127.0.0.1:5000")
    print("=" * 80)
    print("\n✓ Servidor iniciado!")
    print("✓ Abrindo navegador...")
    print("\nPara ENCERRAR: Feche esta janela ou pressione Ctrl+C")
    print("=" * 80)

    threading.Thread(target=open_browser, daemon=True).start()

    try:
        app.run(debug=False, port=5000, use_reloader=False)
    except KeyboardInterrupt:
        print("\n\n✓ Servidor encerrado pelo usuário.")
    except Exception as e:
        logger.error("Erro ao iniciar servidor: %s", e)
        input("\nPressione Enter para sair...")
