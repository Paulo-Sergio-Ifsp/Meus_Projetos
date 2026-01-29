// Funções utilitárias
async function fetchAPI(endpoint, method = "GET", data = null) {
    const options = { method };
    if (data) options.body = JSON.stringify(data);
    const response = await fetch(`/api${endpoint}`, options);
    if (!response.ok) throw new Error(`API Error: ${response.status}`);
    return response.json();
}

function showElement(id) { document.getElementById(id).style.display = "block"; }
function hideElement(id) { document.getElementById(id).style.display = "none"; }

document.addEventListener("DOMContentLoaded", () => { loadDownloads(); });

document.getElementById("book-url").addEventListener("change", async () => {
    const url = document.getElementById("book-url").value;
    if (!url) return;
    try {
        const result = await fetchAPI("/detect-library", "POST", { url });
        const libraryMap = { pearson: "Pearson - UNIVESP", minha: "Minha Biblioteca", evolution: "Evolution" };
        document.getElementById("detected-lib").textContent = libraryMap[result.library] || result.library;
        showElement("library-info");
    } catch (e) {
        hideElement("library-info");
        console.error("Erro ao detectar biblioteca:", e);
    }
});

document.getElementById("start-download").addEventListener("click", async () => {
    const url = document.getElementById("book-url").value;
    const author = document.getElementById("book-author").value;
    const title = document.getElementById("book-title").value;
    if (!url || !author || !title) { alert("Preencha todos os campos do livro!"); return; }
    try {
        console.log("Iniciando download...");
        const libResult = await fetchAPI("/detect-library", "POST", { url });
        const library = libResult.library;
        console.log("Biblioteca detectada:", library);
        const response = await fetchAPI("/download", "POST", { library, url, author, title });
        const downloadId = response.download_id;
        console.log("Download ID:", downloadId);
        showElement("progress-section");
        monitorDownload(downloadId);
    } catch (e) {
        console.error("Erro ao iniciar download:", e);
        alert("Erro ao iniciar download: " + e.message);
    }
});

async function monitorDownload(downloadId) {
    const maxAttempts = 600;
    let attempt = 0;
    console.log("Monitorando download:", downloadId);
    const pollInterval = setInterval(async () => {
        attempt++;
        try {
            const status = await fetchAPI(`/download-status/${downloadId}`);
            console.log("Status:", status);
            const msg = document.getElementById("progress-message");
            const fill = document.getElementById("progress-fill");
            if (msg) msg.textContent = status.message || "Processando...";
            if (fill) {
                fill.textContent = status.progress + "%";
                fill.style.width = status.progress + "%";
            }
            if (status.status === "completo") {
                clearInterval(pollInterval);
                showDownloadResult(status.pdf_url || "/api/download-pdf/" + status.filename);
                loadDownloads();
            } else if (status.status === "erro") {
                clearInterval(pollInterval);
                showError(status.message || "Erro desconhecido");
            } else if (attempt > maxAttempts) {
                clearInterval(pollInterval);
                showError("Timeout - download levou muito tempo");
            }
        } catch (e) {
            console.error("Erro ao monitorar:", e);
            if (attempt > maxAttempts) {
                clearInterval(pollInterval);
                showError("Erro ao monitorar download: " + e.message);
            }
        }
    }, 1000);
}

function showDownloadResult(pdfUrl) {
    console.log("Resultado:", pdfUrl);
    const link = document.getElementById("download-link");
    if (link) link.href = pdfUrl;
    showElement("download-result");
    setTimeout(() => { hideElement("progress-section"); }, 2000);
}

function showError(message) {
    console.error("Erro:", message);
    const section = document.getElementById("progress-section");
    const oldErrors = section.querySelectorAll(".error-message");
    oldErrors.forEach(el => el.remove());
    const errorDiv = document.createElement("div");
    errorDiv.className = "error-message";
    errorDiv.textContent = "✗ " + message;
    errorDiv.style.color = "red";
    errorDiv.style.padding = "10px";
    errorDiv.style.margin = "10px 0";
    errorDiv.style.border = "1px solid red";
    errorDiv.style.borderRadius = "4px";
    section.appendChild(errorDiv);
}

async function loadDownloads() {
    try {
        const result = await fetchAPI("/list-downloads");
        const listDiv = document.getElementById("downloads-list");
        if (result.pdfs && result.pdfs.length > 0) {
            listDiv.innerHTML = result.pdfs.map(pdf => `<div class="download-item"><div class="download-item-info"><div class="download-item-name">${pdf.filename}</div></div><a href="${pdf.url}" class="download-item-link" download>Download</a></div>`).join("");
        } else {
            listDiv.innerHTML = '<p>Nenhum livro baixado ainda</p>';
        }
    } catch (e) {
        console.error("Erro ao carregar downloads:", e);
    }
}

setInterval(loadDownloads, 10000);
