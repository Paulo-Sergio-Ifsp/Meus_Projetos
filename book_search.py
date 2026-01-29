"""
Buscador universal de livros gratuitos
Busca em múltiplas fontes: Archive.org, Open Library, etc
"""

# pylint: disable=broad-exception-caught

import logging
import time
import urllib.parse
from typing import List, Dict

import requests
from bs4 import BeautifulSoup

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class BookSearcher:
    """Busca livros em múltiplas fontes gratuitas"""

    def __init__(self):
        self.session = requests.Session()
        self.session.headers.update(
            {
                "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
            }
        )

    def search(self, title: str, author: str = "") -> List[Dict]:
        """
        Busca livros em múltiplas fontes
        Retorna lista de dicionários com: {title, author, format, url, source, size}
        """
        results = []

        # Buscar em Archive.org
        results.extend(self._search_archive_org(title, author))

        # Buscar em Open Library
        results.extend(self._search_open_library(title, author))

        # Buscar no Google (sites brasileiros e gerais)
        results.extend(self._search_google(title, author))

        # Buscar em sites específicos brasileiros
        results.extend(self._search_brazilian_sites(title, author))

        # Buscar em Z-Library
        results.extend(self._search_zlibrary(title, author))

        # Buscar em LibGen
        results.extend(self._search_libgen(title, author))

        # Buscar em Google Books
        results.extend(self._search_google_books(title, author))

        # Buscar em Kufunda.net (com extração automática de download)
        results.extend(self._search_kufunda_net(title, author))

        # Remover duplicatas baseado na URL
        seen_urls = set()
        unique_results = []
        for result in results:
            if result["url"] not in seen_urls:
                seen_urls.add(result["url"])
                unique_results.append(result)

        # Filtrar resultados por relevância (título deve conter ao menos 1 palavra da busca)
        if title:
            title_words = set(title.lower().split())
            # Remover palavras muito comuns que não ajudam na relevância
            common_words = {
                "the",
                "a",
                "an",
                "and",
                "or",
                "but",
                "in",
                "on",
                "at",
                "to",
                "for",
                "of",
                "with",
                "by",
                "from",
                "up",
                "about",
                "into",
                "through",
                "during",
                "o",
                "e",
                "de",
                "da",
                "do",
                "dos",
                "das",
                "em",
                "para",
                "com",
                "por",
            }
            title_words = title_words - common_words

            # Se a busca tem múltiplas palavras, exigir mais correspondências
            num_search_words = len(title_words)
            original_title = title.lower()

            filtered_results = []
            for result in unique_results:
                curr_title = result["title"].lower()
                result_title_words = set(curr_title.split())

                # Prioridade 1: Busca exata da frase completa
                if original_title in curr_title:
                    filtered_results.append(result)
                    continue

                # Calcular quantas palavras significativas coincidem
                matching_words = title_words & result_title_words
                num_matches = len(matching_words)

                # Critério de relevância dinâmico baseado no tamanho da busca:
                if num_search_words == 1:
                    # Busca de 1 palavra: precisa ter essa palavra
                    if num_matches >= 1:
                        filtered_results.append(result)
                elif num_search_words == 2:
                    # Busca de 2 palavras: precisa ter pelo menos 1
                    if num_matches >= 1:
                        filtered_results.append(result)
                else:  # 3+ palavras
                    # Busca de 3+ palavras: precisa ter pelo menos 2 palavras OU 50% delas
                    min_required = max(2, num_search_words // 2)
                    if num_matches >= min_required:
                        filtered_results.append(result)

            min_val = max(2, num_search_words // 2) if num_search_words > 2 else 1
            logger.info(
                "Filtro relevância: %d -> %d resultados "
                "(busca: %d palavras, min: %d coincidências)",
                len(unique_results),
                len(filtered_results),
                num_search_words,
                min_val,
            )

            # Se filtrou demais (menos de 3 resultados), retornar os primeiros originais
            if len(filtered_results) < 3:
                return unique_results[:15]

            return filtered_results

        return unique_results

    def _search_archive_org(self, title: str, author: str = "") -> List[Dict]:
        """Busca no Internet Archive com melhor relevância"""
        results = []

        try:
            query = f"{title} {author}".strip()
            url = "https://archive.org/advancedsearch.php"

            # Melhorar query para resultados mais relevantes
            search_query = f"title:({title})"
            if author:
                search_query += f" AND creator:({author})"
            search_query += " AND mediatype:texts"

            params = {
                "q": search_query,
                "fl[]": ["identifier", "title", "creator", "format", "downloads"],
                "rows": 25,  # Reduzir para 25 resultados mais relevantes
                "page": 1,
                "output": "json",
                "sort[]": "downloads desc",  # Ordenar por downloads (mais populares primeiro)
            }

            logger.info("Buscando no Archive.org: %s", query)
            response = self.session.get(url, params=params, timeout=15)
            response.raise_for_status()
            data = response.json()

            docs = data.get("response", {}).get("docs", [])
            logger.info("Archive.org retornou %d documentos", len(docs))

            for doc in docs:
                identifier = doc.get("identifier")
                formats = doc.get("format", [])

                if not identifier:
                    continue

                # Verificar formatos disponíveis
                for fmt in ["PDF", "EPUB", "MOBI"]:
                    if fmt in formats or fmt.lower() in [
                        str(f).lower() for f in formats
                    ]:
                        results.append(
                            {
                                "title": doc.get("title", "Unknown"),
                                "author": (
                                    doc.get("creator", ["Unknown"])[0]
                                    if isinstance(doc.get("creator"), list)
                                    else doc.get("creator", "Unknown")
                                ),
                                "format": fmt,
                                "url": (
                                    "https://archive.org/download/"
                                    f"{identifier}/{identifier}.{fmt.lower()}"
                                ),
                                "source": "Archive.org",
                                "size": "Unknown",
                                "identifier": identifier,
                            }
                        )
                        break  # Apenas um formato por livro

            logger.info("Archive.org: %d resultados processados", len(results))
        except Exception as e:
            logger.error("Erro ao buscar no Archive.org: %s", e)

        return results

    def _search_libgen(self, title: str, author: str = "") -> List[Dict]:
        """Busca no Library Genesis"""
        results = []

        try:
            query = f"{title} {author}".strip()

            # LibGen tem várias APIs/mirrors
            mirrors = [
                "https://libgen.is/search.php?req={query}&res=100",
                "https://libgen.rs/search.php?req={query}&res=100",
            ]

            for mirror in mirrors:
                try:
                    search_url = mirror.format(query=urllib.parse.quote(query))
                    logger.info("Buscando em LibGen: %s", mirror.split("/")[2])

                    response = self.session.get(search_url, timeout=15)
                    if response.status_code != 200:
                        continue

                    soup = BeautifulSoup(response.text, "html.parser")

                    # LibGen usa tabelas para resultados
                    rows = soup.select("table.c tr")[1:]  # Pular cabeçalho

                    for row in rows[:20]:  # Máximo 20 resultados
                        cols = row.find_all("td")
                        if len(cols) < 5:
                            continue

                        # Extrair informações
                        title_elem = cols[2].find("a")
                        if not title_elem:
                            continue

                        book_title = title_elem.get_text().strip()
                        author_text = (
                            cols[1].get_text().strip() if len(cols) > 1 else "Unknown"
                        )

                        # Procurar link de download
                        download_links = row.find_all("a", href=True)
                        download_url = None
                        for link in download_links:
                            href = link.get("href")
                            href_str = href if isinstance(href, str) else ""
                            if "download" in href_str.lower() or "get.php" in href_str:
                                download_url = href_str
                                if download_url and not download_url.startswith("http"):
                                    base = "/".join(mirror.split("/")[:3])
                                    download_url = base + download_url
                                break

                        if download_url:
                            # Detectar formato
                            fmt = "PDF"
                            if "epub" in download_url.lower():
                                fmt = "EPUB"
                            elif "mobi" in download_url.lower():
                                fmt = "MOBI"

                            results.append(
                                {
                                    "title": book_title,
                                    "author": author_text,
                                    "format": fmt,
                                    "url": download_url,
                                    "source": "LibGen",
                                    "size": "Unknown",
                                }
                            )

                    if results:
                        break  # Se encontrou, não precisa tentar outros mirrors

                    time.sleep(1)
                except Exception as e:
                    logger.debug("Mirror LibGen %s falhou: %s", mirror, e)
                    continue

            logger.info("LibGen: %d resultados", len(results))
        except Exception as e:
            logger.error("Erro ao buscar no LibGen: %s", e)

        return results

    def _search_google_books(self, title: str, author: str = "") -> List[Dict]:
        """Busca na API do Google Books (apenas links de preview/free)"""
        results = []

        try:
            query = f"{title} {author}".strip()
            url = "https://www.googleapis.com/books/v1/volumes"
            params = {
                "q": query,
                "maxResults": 20,
                "printType": "books",
            }

            logger.info("Buscando em Google Books API")
            response = self.session.get(url, params=params, timeout=10)
            data = response.json()

            for item in data.get("items", []):
                vol_info = item.get("volumeInfo", {})
                access_info = item.get("accessInfo", {})

                # Apenas livros com acesso total ou de preview
                if access_info.get("viewability") in ["ALL_PAGES", "PUBLIC_DOMAIN"]:
                    # Verificar se tem PDF disponível
                    pdf_link = access_info.get("pdf", {}).get(
                        "downloadLink"
                    ) or access_info.get("pdf", {}).get("acsTokenLink")

                    epub_link = access_info.get("epub", {}).get(
                        "downloadLink"
                    ) or access_info.get("epub", {}).get("acsTokenLink")

                    if pdf_link:
                        results.append(
                            {
                                "title": vol_info.get("title", "Unknown"),
                                "author": ", ".join(
                                    vol_info.get("authors", ["Unknown"])
                                ),
                                "format": "PDF",
                                "url": pdf_link,
                                "source": "Google Books",
                                "size": "Unknown",
                            }
                        )
                    elif epub_link:
                        results.append(
                            {
                                "title": vol_info.get("title", "Unknown"),
                                "author": ", ".join(
                                    vol_info.get("authors", ["Unknown"])
                                ),
                                "format": "EPUB",
                                "url": epub_link,
                                "source": "Google Books",
                                "size": "Unknown",
                            }
                        )

            logger.info("Google Books: %d resultados", len(results))
        except Exception as e:
            logger.error("Erro ao buscar em Google Books: %s", e)

        return results

    def _search_google(self, title: str, author: str = "") -> List[Dict]:
        """Busca usando DuckDuckGo e busca direta em sites conhecidos"""
        results = []

        # Lista de sites diretos para verificar
        direct_sites = [
            {
                "name": "PDF Drive",
                "url": "https://www.pdfdrive.com/search?q={query}",
                "item_selector": ".file-right",
                "title_selector": "h2 a",
                "link_selector": "h2 a",
            },
            {
                "name": "Academia.edu",
                "url": "https://www.academia.edu/search?q={query}",
                "item_selector": ".u-borderBottom",
                "title_selector": ".work-title",
                "link_selector": "a.work-title",
            },
        ]

        query = f"{title} {author}".strip()

        # Buscar em cada site diretamente
        for site in direct_sites:
            try:
                search_url = site["url"].format(query=urllib.parse.quote(query))
                logger.info("Buscando em %s", site["name"])

                headers = {
                    "User-Agent": (
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                        "AppleWebKit/537.36 (KHTML, like Gecko) "
                        "Chrome/120.0.0.0 Safari/537.36"
                    ),
                    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                    "Accept-Language": "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7",
                }

                response = self.session.get(search_url, headers=headers, timeout=15)
                if response.status_code != 200:
                    continue

                soup = BeautifulSoup(response.text, "html.parser")
                items = soup.select(site["item_selector"])

                for item in items[:10]:
                    title_elem = item.select_one(site["title_selector"])
                    link_elem = item.select_one(site["link_selector"])

                    if title_elem and link_elem:
                        book_url = link_elem.get("href")
                        book_url = book_url if isinstance(book_url, str) else ""
                        if book_url and not book_url.startswith("http"):
                            base_url = "/".join(search_url.split("/")[:3])
                            book_url = base_url + book_url

                        results.append(
                            {
                                "title": title_elem.get_text().strip(),
                                "author": author or "Unknown",
                                "format": "PDF",
                                "url": book_url,
                                "source": site["name"],
                                "size": "Unknown",
                            }
                        )

                time.sleep(1)  # Evitar rate limiting
            except Exception as e:
                logger.debug("Erro ao buscar em %s: %s", site["name"], e)

        logger.info("Busca direta em sites: %d resultados", len(results))
        return results[:20]  # Limitar a 20 resultados

    def _search_brazilian_sites(self, title: str, author: str = "") -> List[Dict]:
        """Busca em sites brasileiros específicos"""
        results = []
        query = f"{title} {author}".strip()

        # Adicionar mais resultados do Archive.org expandindo a busca
        try:
            logger.info("Expandindo busca no Archive.org (português)")
            url = "https://archive.org/advancedsearch.php"
            params = {
                "q": f"({query}) AND (language:por OR language:portuguese)",
                "fl[]": ["identifier", "title", "creator", "format"],
                "rows": 100,
                "page": 1,
                "output": "json",
            }

            response = self.session.get(url, params=params, timeout=15)
            if response.status_code == 200:
                data = response.json()
                docs = data.get("response", {}).get("docs", [])

                for doc in docs:
                    identifier = doc.get("identifier")
                    formats = doc.get("format", [])

                    if not identifier:
                        continue

                    # Tentar todos os formatos disponíveis
                    for fmt in ["PDF", "EPUB", "MOBI", "Text PDF"]:
                        if any(fmt.lower() in str(f).lower() for f in formats):
                            results.append(
                                {
                                    "title": doc.get("title", "Unknown"),
                                    "author": (
                                        doc.get("creator", ["Unknown"])[0]
                                        if isinstance(doc.get("creator"), list)
                                        else doc.get("creator", "Unknown")
                                    ),
                                    "format": fmt.replace(" ", "_"),
                                    "url": (
                                        "https://archive.org/download/"
                                        f"{identifier}/"
                                        f"{identifier}.{fmt.lower().replace(' ', '_')}"
                                    ),
                                    "source": "Archive.org (PT)",
                                    "size": "Unknown",
                                    "identifier": identifier,
                                }
                            )
                            break

                logger.info("Archive.org PT: %d resultados adicionais", len(results))
        except Exception as e:
            logger.debug("Erro na busca expandida: %s", e)

        return results

    def _search_zlibrary(self, title: str, author: str = "") -> List[Dict]:
        """Busca no Z-Library (mirrors públicos)"""
        results = []

        try:
            # Usar mirrors públicos do Z-Library
            mirrors = [
                "https://z-lib.gs",
                "https://zlibrary-global.se",
            ]

            query = f"{title} {author}".strip()

            for mirror in mirrors:
                try:
                    search_url = f"{mirror}/s/{urllib.parse.quote(query)}"
                    logger.info("Buscando em Z-Library: %s", mirror)

                    response = self.session.get(search_url, timeout=15)
                    if response.status_code != 200:
                        continue

                    soup = BeautifulSoup(response.text, "html.parser")
                    books = soup.select(
                        "div.book-item, div.resItemBox, table.resItemTable tr"
                    )

                    for book in books[:10]:
                        title_elem = book.select_one("h3 a, td a, .book-title a")
                        if not title_elem:
                            continue

                        book_url = title_elem.get("href")
                        book_url = book_url if isinstance(book_url, str) else ""
                        if book_url and not book_url.startswith("http"):
                            book_url = mirror + book_url

                        results.append(
                            {
                                "title": title_elem.get_text().strip(),
                                "author": author or "Unknown",
                                "format": "PDF/EPUB/MOBI",
                                "url": book_url,
                                "source": "Z-Library",
                                "size": "Unknown",
                            }
                        )

                    if results:
                        break  # Se encontrou em um mirror, não precisa tentar outros

                    time.sleep(1)
                except Exception as e:
                    logger.debug("Mirror %s não disponível: %s", mirror, e)
                    continue

        except Exception as e:
            logger.error("Erro ao buscar no Z-Library: %s", e)

        return results

    def _search_open_library(self, title: str, author: str = "") -> List[Dict]:
        """Busca na Open Library"""
        results = []

        try:
            query = f"{title} {author}".strip()
            url = "https://openlibrary.org/search.json"
            params = {"q": query, "limit": 20}

            response = self.session.get(url, params=params, timeout=10)
            data = response.json()

            for doc in data.get("docs", []):
                # Open Library tem links para Archive.org
                if doc.get("ia"):
                    for ia_id in doc.get("ia", [])[:1]:  # Pegar primeiro ID
                        results.append(
                            {
                                "title": doc.get("title", "Unknown"),
                                "author": (
                                    doc.get("author_name", ["Unknown"])[0]
                                    if doc.get("author_name")
                                    else "Unknown"
                                ),
                                "format": "PDF",
                                "url": f"https://archive.org/download/{ia_id}/{ia_id}.pdf",
                                "source": "Open Library",
                                "size": "Unknown",
                                "identifier": ia_id,
                            }
                        )

            logger.info("Open Library: %d resultados", len(results))
        except Exception as e:
            logger.error("Erro ao buscar na Open Library: %s", e)

        return results

    def download_book(self, book: Dict, output_path: str) -> bool:
        """Faz download de um livro"""
        try:
            logger.info("Baixando: %s (%s)", book["title"], book["format"])

            url = book.get("url")

            if not url:
                logger.error("❌ URL vazia para o livro: %s", book.get("title"))
                return False

            logger.info("URL original: %s", url)

            # IMPORTANTE: Remover fragmentos de URL (tudo após #) que quebram downloads
            # Ex: "arquivo.pdf#PAGINA.pdf" -> "arquivo.pdf"
            if "#" in url:
                url = url.split("#")[0]
                logger.info("URL limpa (removido fragmento): %s", url)

            # Resolver URLs reais do Archive.org/Open Library para evitar 404
            if book.get("source") in [
                "Archive.org",
                "Archive.org (PT)",
                "Open Library",
            ]:
                identifier = book.get("identifier")
                if identifier:
                    resolved_url = self._resolve_archive_download_url(
                        identifier, book.get("format", "PDF")
                    )
                    if resolved_url:
                        url = resolved_url
                        logger.info("URL resolvida (Archive.org): %s", url)

            if not url:
                logger.error("❌ Não foi possível resolver URL para download")
                return False

            logger.info("🔽 Iniciando download de: %s", url)
            response = self.session.get(url, stream=True, timeout=30)
            response.raise_for_status()

            total_size = int(response.headers.get("content-length", 0))
            downloaded = 0

            with open(output_path, "wb") as f:
                for chunk in response.iter_content(chunk_size=8192):
                    if chunk:
                        f.write(chunk)
                        downloaded += len(chunk)
                        if total_size > 0:
                            progress = (downloaded / total_size) * 100
                            logger.debug("Progresso: %.1f%%", progress)

            logger.info("✓ Download concluído: %s", output_path)
            return True

        except Exception as e:
            logger.error(
                "❌ Erro ao baixar %s: %s", book.get("title"), e, exc_info=True
            )
            return False

    def _resolve_archive_download_url(
        self, identifier: str, preferred_format: str
    ) -> str:
        """Resolve o arquivo correto no Archive.org via metadata"""
        try:
            meta_url = f"https://archive.org/metadata/{identifier}"
            response = self.session.get(meta_url, timeout=15)
            if response.status_code != 200:
                return ""

            data = response.json()
            files = data.get("files", [])

            # Normalizar formato preferido
            pref = (preferred_format or "").lower().replace("_", " ")

            # Prioridade por extensão
            ext_priority = []
            if "epub" in pref:
                ext_priority = [".epub"]
            elif "mobi" in pref:
                ext_priority = [".mobi", ".azw3"]
            else:
                ext_priority = [".pdf"]

            # 1) Tentar por extensão no nome do arquivo
            for ext in ext_priority:
                for f in files:
                    name = str(f.get("name", ""))
                    if name.lower().endswith(ext):
                        return f"https://archive.org/download/{identifier}/{name}"

            # 2) Tentar por campo format (ex: "Text PDF")
            for f in files:
                name = str(f.get("name", ""))
                fmt = str(f.get("format", "")).lower()
                if "pdf" in pref and "pdf" in fmt:
                    return f"https://archive.org/download/{identifier}/{name}"
                if "epub" in pref and "epub" in fmt:
                    return f"https://archive.org/download/{identifier}/{name}"
                if "mobi" in pref and "mobi" in fmt:
                    return f"https://archive.org/download/{identifier}/{name}"

            # 3) Fallback: primeiro arquivo com extensão conhecida
            for f in files:
                name = str(f.get("name", ""))
                if name.lower().endswith((".pdf", ".epub", ".mobi", ".azw3")):
                    return f"https://archive.org/download/{identifier}/{name}"

        except Exception as e:
            logger.debug("Falha ao resolver arquivo do Archive.org: %s", e)

        return ""

    def _search_kufunda_net(self, title: str, author: str = "") -> List[Dict]:
        """Busca em Kufunda.net e extrai links diretos de download"""
        results = []

        try:
            query = f"{title} {author}".strip()

            # URL correta do buscador do Kufunda.net
            search_url = (
                f"https://www.kufunda.net/results.php?query={urllib.parse.quote(query)}"
            )

            logger.info("Buscando em Kufunda.net: %s", query)

            headers = {
                "User-Agent": (
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                    "AppleWebKit/537.36 (KHTML, like Gecko) "
                    "Chrome/120.0.0.0 Safari/537.36"
                ),
                "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                "Accept-Language": "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7",
                "Referer": "https://www.kufunda.net/",
            }

            # Passo 1: Buscar na página de resultados
            response = self.session.get(search_url, headers=headers, timeout=15)

            if response.status_code != 200:
                logger.warning("Kufunda.net retornou status %d", response.status_code)
                return results

            soup = BeautifulSoup(response.text, "html.parser")

            # Passo 2: Kufunda fornece links diretos para PDFs na página de resultados
            # Procurar por links que terminam em .pdf
            pdf_links = soup.find_all(
                "a", href=lambda x: bool(x and ".pdf" in x.lower())
            )

            logger.debug("Kufunda encontrou %d links de PDF", len(pdf_links))

            # Processar cada link de PDF encontrado
            for pdf_link in pdf_links[:15]:  # Limitar a 15 para não demorar muito
                try:
                    pdf_url = pdf_link.get("href", "")
                    if not isinstance(pdf_url, str) or not pdf_url:
                        continue

                    # Completar URL se for relativa
                    if not pdf_url.startswith("http"):
                        pdf_url = "https://www.kufunda.net/" + pdf_url.lstrip("/")

                    # Extrair título do link ou do PDF
                    book_title = pdf_link.get_text().strip()
                    if not book_title or book_title.upper() == "BAIXAR":
                        # Tentar extrair do caminho do arquivo
                        book_title = (
                            pdf_url.split("/")[-1]
                            .replace(".pdf", "")
                            .replace("-", " ")
                            .title()
                        )

                    if not book_title or len(book_title) < 3:
                        continue

                    results.append(
                        {
                            "title": book_title,
                            "author": author or "Unknown",
                            "url": pdf_url,
                            "format": "PDF",
                            "source": "Kufunda.net",
                        }
                    )

                except Exception as e:
                    logger.debug("Erro ao processar PDF do Kufunda: %s", e)
                    continue

            logger.info("Kufunda.net: %d resultados", len(results))

        except Exception as e:
            logger.error("Erro na busca Kufunda.net: %s", e)

        return results
