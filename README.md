# 📚 meu_Busca_Livros
Buscador universal de livros gratuitos em fontes públicas, com interface web simples.

## 🚀 Como usar (SUPER FÁCIL!)

### Opção 1: Um clique apenas! ⚡
1. **Dê duplo clique** no arquivo `Iniciar_Busca_Livros.bat`
2. Aguarde o navegador abrir automaticamente
3. Digite o nome do livro e clique em "Buscar"
4. Pronto! 🎉

### Opção 2: Manual (se preferir)
1. Abra o terminal/prompt nesta pasta
2. Execute: `python app_search.py`
3. Abra seu navegador em: http://127.0.0.1:5000

## 📋 Requisitos
- **Python 3.8 ou superior** instalado
- Conexão com a internet

### Primeira vez usando?
Na **primeira execução**, o sistema instalará automaticamente as dependências necessárias. Isso pode levar alguns minutos.

## ✅ Funcionalidades
- Busca em múltiplas fontes públicas:
  - Archive.org (Internet Archive)
  - Open Library
  - Google Books
  - Kufunda.net
  - Z-Library
  - LibGen
- Download direto quando houver arquivo disponível
- Interface web moderna e responsiva
- Filtro inteligente de relevância
- Sem necessidade de login

## 📥 Downloads
Os livros baixados são salvos automaticamente em: `G:\Livros_Archive`

Para alterar a pasta de downloads, edite a variável `DEFAULT_DOWNLOAD_DIR` no arquivo `app_search.py` (linha 22).

## ⚠️ Importante
- Este projeto busca **apenas fontes públicas e abertas**
- Respeite os direitos autorais e as políticas de cada site
- Alguns livros podem estar protegidos por direitos autorais
- Use apenas para fins educacionais e pessoais

## 🐛 Problemas comuns

### "Python não encontrado"
- Instale o Python de: https://www.python.org/downloads/
- **IMPORTANTE**: Marque a opção "Add Python to PATH" durante a instalação

### "Erro ao instalar dependências"
- Certifique-se de estar conectado à internet
- Execute como administrador (clique com botão direito → "Executar como administrador")

### "Navegador não abre automaticamente"
- Abra manualmente: http://127.0.0.1:5000
- Verifique se a porta 5000 não está sendo usada por outro programa

## 🛑 Como encerrar
- Feche a janela do terminal/prompt
- Ou pressione `Ctrl+C` no terminal

## 📝 Notas técnicas
- Servidor de desenvolvimento Flask (não usar em produção)
- CORS habilitado para desenvolvimento
- Logs em nível INFO para debug

---

**Versão:** 2.0
**Última atualização:** 29/01/2026
