"""
Fetcher legado - Migração para Python
Substitui PhantomJS por Selenium + Chrome Headless
"""

# pylint: disable=broad-exception-caught

import logging
import os
import time
import json
import tempfile
from pathlib import Path
from typing import List, Optional, Tuple

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.options import Options
import requests
from PIL import Image

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class LibraryFetcher:
    """Classe base para download de livros de bibliotecas"""

    def __init__(self, username: str, password: str, headless: bool = True):
        self.username = username
        self.password = password
        self.headless = headless
        self.driver = self._init_driver(headless)
        self.session = requests.Session()

    def _init_driver(self, headless: bool) -> webdriver.Chrome:
        """Inicializa Selenium WebDriver com Chrome"""
        options = Options()
        if headless:
            options.add_argument("--headless")
        profile_dir = os.environ.get("CHROME_PROFILE_DIR")
        if profile_dir:
            options.add_argument(f"--user-data-dir={profile_dir}")
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")
        options.add_argument("--disable-blink-features=AutomationControlled")
        options.add_experimental_option("excludeSwitches", ["enable-automation"])
        options.add_experimental_option("useAutomationExtension", False)

        driver = webdriver.Chrome(options=options)

        # Carregar cookies salvos se existirem
        self._load_saved_cookies(driver)

        return driver

    def _load_saved_cookies(self, driver: webdriver.Chrome):
        """Carrega cookies salvos de uma sessão anterior"""
        # Procurar em múltiplos locais
        possible_paths = [
            Path("minha_biblioteca_cookies.json"),
            Path(tempfile.gettempdir()) / "minha_biblioteca_cookies.json",
        ]

        cookie_file = None
        for path in possible_paths:
            if path.exists():
                cookie_file = path
                break

        if not cookie_file:
            return

        try:
            with open(cookie_file, "r", encoding="utf-8") as f:
                cookies = json.load(f)

            # Precisa acessar um domínio primeiro para adicionar cookies
            driver.get("https://integrada.minhabiblioteca.com.br")

            for cookie in cookies:
                try:
                    driver.add_cookie(cookie)
                except Exception as e:
                    logger.debug(
                        "Não foi possível adicionar cookie %s: %s",
                        cookie.get("name"),
                        e,
                    )

            logger.info("✓ Cookies salvos carregados de: %s", cookie_file)
        except Exception as e:
            logger.warning("Não foi possível carregar cookies: %s", e)

    def login(self) -> bool:
        """Autentica na biblioteca. Deve ser sobrescrito em subclasses.

        Returns:
            True se autenticado com sucesso, False caso contrário
        """
        raise NotImplementedError

    def get_page_range(self, book_url: str) -> Tuple[int, int]:
        """Detecta intervalo de páginas do livro.

        Args:
            book_url: URL do livro

        Returns:
            Tupla (primeira_página, última_página)
        """
        raise NotImplementedError

    def download_pages(
        self, book_url: str, start: int, end: int, output_dir: str
    ) -> Optional[List[str]]:
        """Baixa imagens de todas as páginas especificadas.

        Args:
            book_url: URL do livro
            start: Número da primeira página
            end: Número da última página
            output_dir: Diretório para salvar imagens

        Returns:
            Lista de caminhos das imagens baixadas, ou None em erro
        """
        raise NotImplementedError

    def create_pdf(self, image_dir: str, output_path: str) -> bool:
        """Cria PDF a partir das imagens no diretório especificado.

        Args:
            image_dir: Diretório contendo imagens JPG
            output_path: Caminho do arquivo PDF de saída

        Returns:
            True se PDF foi criado com sucesso, False caso contrário
        """
        try:
            images = sorted(Path(image_dir).glob("*.jpg"))
            if not images:
                logger.error("Nenhuma imagem encontrada em %s", image_dir)
                return False

            pil_images = [Image.open(img).convert("RGB") for img in images]
            pil_images[0].save(output_path, save_all=True, append_images=pil_images[1:])
            logger.info("✓ PDF criado: %s", output_path)
            return True
        except Exception as e:
            logger.error("Erro ao criar PDF: %s", e)
            return False

    def close(self):
        """Fecha o driver"""
        self.driver.quit()


class PearsonFetcher(LibraryFetcher):
    """Fetcher para Pearson"""

    LOGIN_URL = "https://login.univesp.br/simplesaml/module.php/core/pearson.php"

    def login(self) -> bool:
        """Faz login na Pearson"""
        try:
            logger.info("Fazendo login na Pearson...")
            self.driver.get(self.LOGIN_URL)
            time.sleep(2)  # Aguarda carregamento da página

            # Aguarda elementos ficarem clicáveis
            username_elem = WebDriverWait(self.driver, 15).until(
                EC.element_to_be_clickable((By.ID, "username"))
            )
            password_elem = WebDriverWait(self.driver, 15).until(
                EC.element_to_be_clickable((By.ID, "password"))
            )

            # Preenche via JS para evitar problemas de foco/overlay
            self.driver.execute_script(
                "arguments[0].value = arguments[1]; "
                "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));",
                username_elem,
                self.username,
            )
            time.sleep(0.3)
            self.driver.execute_script(
                "arguments[0].value = arguments[1]; "
                "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));",
                password_elem,
                self.password,
            )
            time.sleep(0.3)
            self.driver.execute_script(
                "arguments[0].click();",
                self.driver.find_element(By.ID, "regularsubmit"),
            )

            # Aguarda redirecionamento após login (máximo 20 segundos)
            time.sleep(3)  # Aguarda início do redirecionamento

            # Verifica se conseguiu fazer login
            for _ in range(40):  # 40 x 0.5s = 20s total
                current_url = self.driver.current_url
                if (
                    "login" not in current_url.lower()
                    or "simplesaml" not in current_url.lower()
                ):
                    logger.info("✓ Login bem-sucedido na Pearson!")
                    return True
                time.sleep(0.5)

            # Se chegou aqui, algo deu errado
            logger.error("Login Pearson falhou. URL final: %s", self.driver.current_url)
            return False
        except Exception as e:
            logger.error("Erro no login Pearson: %s", e)
            return False

    def get_page_range(self, book_url: str) -> Tuple[int, int]:
        """Detecta primeira e última página da Pearson via busca binária"""
        try:
            self.driver.get(book_url)
            time.sleep(3)

            # Extrai primeira página da URL
            url_parts = self.driver.current_url.split("/")
            first_page = int(url_parts[-1]) if url_parts[-1].isdigit() else 1

            # Busca binária pela última página
            last_page = self._binary_search_last_page(book_url, first_page)
            logger.info("Páginas detectadas: %s - %s", first_page, last_page)
            return first_page, last_page
        except (OSError, ValueError) as e:
            logger.error("Erro ao detectar páginas: %s", e)
            return 1, 1

    def _binary_search_last_page(
        self, base_url: str, first_page: int, low: int = 1, high: int = 500
    ) -> int:
        """Busca binária para encontrar última página"""
        if low > high:
            return low - 1

        mid = (low + high) // 2
        test_url = f"{base_url}/pages/{mid}"

        try:
            self.driver.get(test_url)
            time.sleep(1)

            # Verifica se página é válida pelo conteúdo
            if (
                "pbk-page" in self.driver.page_source
                or "Img" in self.driver.page_source
            ):
                return self._binary_search_last_page(
                    base_url, first_page, mid + 1, high
                )
            else:
                return self._binary_search_last_page(base_url, first_page, low, mid - 1)
        except Exception:
            return self._binary_search_last_page(base_url, first_page, low, mid - 1)

    def download_pages(
        self, book_url: str, start: int, end: int, output_dir: str
    ) -> Optional[List[str]]:
        """Baixa páginas da Pearson"""
        Path(output_dir).mkdir(parents=True, exist_ok=True)
        downloaded = []

        for page_num in range(
            start, end + 1, 2
        ):  # Pearson tem 2 páginas por requisição
            try:
                url = f"{book_url}/pages/{page_num}"
                self.driver.get(url)
                time.sleep(2)

                # Extrai URLs das imagens
                imgs = self.driver.execute_script(
                    """
                    let imgs = document.querySelectorAll('img[class*="Img"]');
                    return Array.from(imgs).map(i => i.src).filter(s => s.includes('http'));
                """
                )

                for idx, img_url in enumerate(imgs):
                    if img_url:
                        resp = self.session.get(img_url, timeout=10)
                        filepath = Path(output_dir) / f"{page_num:05d}_{idx}.jpg"
                        filepath.write_bytes(resp.content)
                        downloaded.append(str(filepath))
                        logger.info("Baixada: %s", filepath.name)
            except (OSError, ValueError, RuntimeError) as e:
                logger.error("Erro ao baixar página %s: %s", page_num, e)

        return downloaded


class MinhaFetcher(LibraryFetcher):
    """Fetcher para Minha Biblioteca"""

    LOGIN_URL = "https://integrada.minhabiblioteca.com.br"
    JIGSAW_BASES = [
        "https://jigsaw.minhabiblioteca.com.br",
        "https://jigsaw.vitalsource.com",
    ]
    MINHA_BASE = "https://integrada.minhabiblioteca.com.br/#"
    READER_PREFIX = "https://integrada.minhabiblioteca.com.br/reader/books/"

    def _is_login_page(self, page_source: str, current_url: str) -> bool:
        """Detecta tela de login/sessão expirada"""
        src = page_source.lower()
        url = current_url.lower()
        return (
            "click to sign in" in src
            or "sign in" in src
            or "login" in url
            or "saml" in url
        )

    def _to_jigsaw_url(self, book_url: str, base: str) -> str:
        """Converte URL da Minha Biblioteca para URL Jigsaw"""
        if book_url.startswith(self.READER_PREFIX):
            # Ex: https://integrada.minhabiblioteca.com.br/reader/books/978.../pageid/0
            book_id = book_url.replace(self.READER_PREFIX, "").split("/")[0]
            return f"{base}/books/{book_id}"

        # Formato com hash
        if self.MINHA_BASE in book_url:
            return book_url.replace(self.MINHA_BASE, base + "/books/").split("?")[0]

        return book_url

    def _candidate_jigsaw_urls(self, book_url: str) -> List[str]:
        """Gera possíveis bases Jigsaw para tentativa"""
        normalized = book_url.strip()
        for base in self.JIGSAW_BASES:
            if normalized.startswith(base):
                return [normalized.split("?")[0]]

        return [
            self._to_jigsaw_url(normalized, base).split("?")[0]
            for base in self.JIGSAW_BASES
        ]

    def _page_exists(self, base_url: str, page_num: int) -> bool:
        """Verifica se página existe - suporta PictureBook e HTML Books"""
        test_url = f"{base_url}/{page_num}?jigsaw_brand=integradaminhabiblioteca"
        logger.info("🔍 Testando página: %s", test_url)
        self.driver.get(test_url)
        time.sleep(4)  # Aumentado para canvas renderizar

        page_source = self.driver.page_source
        current_url = self.driver.current_url
        logger.info("   URL atual: %s", current_url)

        if self._is_login_page(page_source, current_url):
            logger.warning("   ❌ Redirecionado para login")
            return False

        # Tipo 1: PictureBook (canvas do VST)
        if "window.innerPageData" in page_source:
            logger.info("   ✓ Página encontrada (PictureBook)")
            return True

        # Tipo 2: HTML Books com pbk-page
        if "pbk-page" in page_source:
            logger.info("   ✓ Página encontrada (HTML Book)")
            return True

        logger.info("   ❌ Página não detectada (sem innerPageData ou pbk-page)")
        return False

    def login(self) -> bool:
        """Verifica se já está logado na Minha Biblioteca (login manual pelo usuário)"""
        try:
            logger.info("Verificando sessão da Minha Biblioteca...")
            if self.headless:
                logger.error(
                    "❌ Modo headless não permite login manual. "
                    "Defina CHROME_PROFILE_DIR ou execute sem headless."
                )
                return False

            logger.info("=" * 80)
            logger.info("⚠️  INSTRUÇÕES PARA LOGIN MANUAL:")
            logger.info("   1. Uma janela do Chrome irá abrir automaticamente")
            logger.info("   2. Acesse o portal institucional nesta janela")
            logger.info("   3. Faça login com suas credenciais")
            logger.info("   4. Clique em 'Minha Biblioteca' no menu")
            logger.info("   5. MANTENHA A JANELA ABERTA até o download concluir")
            logger.info("=" * 80)

            # Acessa Minha Biblioteca diretamente
            self.driver.get(self.LOGIN_URL)
            wait_seconds = int(os.environ.get("LOGIN_WAIT_SECONDS", "600"))
            deadline = time.time() + wait_seconds
            elapsed = 0

            logger.info("⏱️  Aguardando login manual (até %s segundos)...", wait_seconds)

            while time.time() < deadline:
                current_url = self.driver.current_url
                elapsed = int(time.time() - (deadline - wait_seconds))

                if elapsed % 10 == 0:  # Log a cada 10 segundos
                    logger.info(
                        "⏱️  Esperando login... [%ss/%ss] URL: %s",
                        elapsed,
                        wait_seconds,
                        current_url,
                    )

                if (
                    "minhabiblioteca.com.br" in current_url.lower()
                    and "login" not in current_url.lower()
                ):
                    logger.info("✓ Sessão detectada! Usuário já está logado.")
                    return True

                time.sleep(3)

            logger.error("❌ Sessão não encontrada após %s segundos.", wait_seconds)
            logger.error(
                "💡 Dica: Defina LOGIN_WAIT_SECONDS com mais tempo se necessário"
            )
            return False

        except Exception as e:
            logger.error("Erro ao verificar sessão: %s", e)
            return False

    def get_page_range(self, book_url: str) -> Tuple[int, int]:
        """Detecta páginas da Minha Biblioteca"""
        try:
            # Se for URL do reader, abrir primeiro para criar cookies do jigsaw
            if book_url.startswith(self.READER_PREFIX):
                logger.info("🔓 Abrindo livro no leitor para criar sessão no Jigsaw...")
                self.driver.get(book_url)
                time.sleep(6)  # Aguarda o leitor carregar e criar cookies jigsaw
                logger.info("   ✓ Sessão do Jigsaw criada")

            candidates = self._candidate_jigsaw_urls(book_url)
            logger.info("📖 URLs candidatas para o livro: %s", candidates)

            for jigsaw_url in candidates:
                logger.info("🔍 Tentando base: %s", jigsaw_url)
                # Detecta primeira página (alguns livros começam em 0)
                if self._page_exists(jigsaw_url, 1):
                    first_page = 1
                elif self._page_exists(jigsaw_url, 0):
                    first_page = 0
                else:
                    logger.warning("   ⚠️  Nenhuma página encontrada nesta base")
                    continue

                last_page = self._binary_search_minha(jigsaw_url, first_page, 500)
                logger.info("✓ Páginas detectadas: %s - %s", first_page, last_page)
                return first_page, last_page

            raise ValueError(
                "URL inválida para livro. Abra o livro e copie o link da leitura."
            )
        except Exception as e:
            logger.error("Erro ao detectar páginas Minha Biblioteca: %s", e)
            return 1, 10

    def _binary_search_minha(self, base_url: str, low: int, high: int) -> int:
        """Busca binária para Minha Biblioteca"""
        if low > high:
            return max(1, high)  # Nunca retorna menos de 1

        mid = (low + high) // 2
        test_url = f"{base_url}/{mid}?jigsaw_brand=integradaminhabiblioteca"

        try:
            self.driver.get(test_url)
            time.sleep(1)

            if "pbk-page" in self.driver.page_source:
                return self._binary_search_minha(base_url, mid + 1, high)
            else:
                return self._binary_search_minha(base_url, low, mid - 1)
        except Exception:
            return self._binary_search_minha(base_url, low, mid - 1)

    def download_pages(
        self, book_url: str, start: int, end: int, output_dir: str
    ) -> Optional[List[str]]:
        """Baixa páginas da Minha Biblioteca (PictureBook ou HTML)"""
        Path(output_dir).mkdir(parents=True, exist_ok=True)
        downloaded = []

        # Abrir o livro no leitor antes de começar o download para manter sessão ativa
        if book_url.startswith(self.READER_PREFIX):
            logger.info("🔄 Abrindo livro no leitor para manter sessão...")
            self.driver.get(book_url)
            time.sleep(5)

        candidates = self._candidate_jigsaw_urls(book_url)
        jigsaw_base = candidates[0]

        for page_num in range(start, end + 1):
            try:
                url = f"{jigsaw_base}/{page_num}?jigsaw_brand=integradaminhabiblioteca"
                self.driver.get(url)
                time.sleep(3)  # Aguarda renderização do canvas

                if self._is_login_page(
                    self.driver.page_source, self.driver.current_url
                ):
                    logger.error(
                        "Sessão expirada na página %s. Refazendo login no leitor...",
                        page_num,
                    )
                    # Tenta reabrir o livro para renovar sessão
                    if book_url.startswith(self.READER_PREFIX):
                        self.driver.get(book_url)
                        time.sleep(5)
                        # Tenta novamente
                        self.driver.get(url)
                        time.sleep(3)
                        if self._is_login_page(
                            self.driver.page_source, self.driver.current_url
                        ):
                            logger.error("Sessão perdida definitivamente.")
                            return None
                    else:
                        return None

                # Tenta extrair URL da imagem (HTML Books com pbk-page)
                img_url = self.driver.execute_script(
                    """
                    let img = document.querySelector('img[class*="pbk-page"]');
                    return img ? img.src : null;
                """
                )

                # Se não encontrou imagem, faz screenshot (PictureBook)
                if not img_url:
                    filepath = Path(output_dir) / f"{page_num:05d}.jpg"
                    self.driver.save_screenshot(str(filepath))
                    downloaded.append(str(filepath))
                    logger.info("Capturada via screenshot: %s", filepath.name)
                    continue

                # Se encontrou URL da imagem, baixa-a
                if img_url:
                    # Melhora resolução
                    img_url = img_url.replace("800", "1600")
                    resp = self.session.get(img_url, timeout=10)
                    filepath = Path(output_dir) / f"{page_num:05d}.jpg"
                    filepath.write_bytes(resp.content)
                    downloaded.append(str(filepath))
                    logger.info("Baixada: %s", filepath.name)
            except (OSError, ValueError, RuntimeError) as e:
                logger.error("Erro ao baixar página %s: %s", page_num, e)

        return downloaded


class EvolutionFetcher(LibraryFetcher):
    """Fetcher para Evolution"""

    LOGIN_URL = "https://login.univesp.br"

    def login(self) -> bool:
        """Faz login na Evolution (placeholder)"""
        logger.warning("⚠ Login Evolution ainda não implementado")
        return False

    def get_page_range(self, book_url: str) -> Tuple[int, int]:
        logger.warning("⚠ Detection Evolution ainda não implementado")
        return 1, 10

    def download_pages(
        self, book_url: str, start: int, end: int, output_dir: str
    ) -> Optional[List[str]]:
        logger.warning("⚠ Download Evolution ainda não implementado")
        return []


def get_fetcher(library: str, username: str, password: str) -> Optional[LibraryFetcher]:
    """Retorna o fetcher apropriado baseado na biblioteca"""
    fetchers = {
        "pearson": PearsonFetcher,
        "mylib": MinhaFetcher,
        "minha": MinhaFetcher,
        "evolution": EvolutionFetcher,
    }

    fetcher_class = fetchers.get(library.lower())
    if fetcher_class:
        if fetcher_class is MinhaFetcher:
            return fetcher_class(username, password, headless=False)
        return fetcher_class(username, password)
    return None
