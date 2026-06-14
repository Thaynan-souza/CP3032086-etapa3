const express = require('express');
const axios = require('axios');
const path = require('path');

const app = express();
const PORT = 3000;

// Configurações para ler dados de formulários HTML e servir arquivos estáticos
app.use(express.urlencoded({ extended: true }));
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// 1. GET / -> Serve o index.html
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// 2. POST /send-code -> Chama o Java e redireciona para o verify
app.post('/send-code', async (req, res) => {
    const email = req.body.email;
    try {
        await axios.post('http://localhost:8081/auth/request-code', { email });
        // Redireciona passando o email pela URL (Query String)
        res.redirect(`/verify?email=${encodeURIComponent(email)}`);
    } catch (error) {
        console.error("Erro ao solicitar código:", error.message);
        res.status(500).send("Erro ao enviar código. O servidor Java está rodando?");
    }
});

// 3. GET /verify -> Serve o verify.html
app.get('/verify', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'verify.html'));
});

// 4. POST /verify-code -> Valida no Java, guarda o Token e redireciona
app.post('/verify-code', async (req, res) => {
    const { email, code } = req.body;
    try {
        const response = await axios.post('http://localhost:8081/auth/verify-code', { email, code });
        const token = response.data.token;
        
        // Injeta um script no navegador para guardar o JWT no sessionStorage e ir para o dashboard
        res.send(`
            <script>
                sessionStorage.setItem('jwt_token', '${token}');
                window.location.href = '/dashboard';
            </script>
        `);
    } catch (error) {
        res.send(`
            <h3>Código inválido ou expirado.</h3>
            <a href="/verify?email=${encodeURIComponent(email)}">Tentar novamente</a>
        `);
    }
});

// Rota extra: Dashboard apenas para confirmar que o redirecionamento funcionou
app.get('/dashboard', (req, res) => {
    res.send(`
        <h1>Login realizado com sucesso!</h1>
        <p>Abra o console do navegador (F12) > Application > Session Storage para ver o seu Token JWT guardado.</p>
    `);
});

app.listen(PORT, () => {
    console.log(`Frontend rodando em http://localhost:${PORT}`);
});