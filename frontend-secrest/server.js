const express = require('express');
const axios = require('axios');
const path = require('path');

const app = express();
const PORT = 3000;

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
        res.redirect(`/verify?email=${encodeURIComponent(email)}`);
    } catch (error) {
        res.status(500).send("Erro ao enviar código. O servidor Java está rodando?");
    }
});

// 3. GET /verify -> Serve o verify.html
app.get('/verify', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'verify.html'));
});

// 4. POST /verify-code -> Valida e vai para o REGISTER
app.post('/verify-code', async (req, res) => {
    const { email, code } = req.body;
    try {
        const response = await axios.post('http://localhost:8081/auth/verify-code', { email, code });
        const token = response.data.token;
        
        // Guarda o token e redireciona para a tela de completar o perfil!
        res.send(`
            <script>
                sessionStorage.setItem('jwt_token', 'Bearer ${token}');
                window.location.href = '/register';
            </script>
        `);
    } catch (error) {
        res.send(`<h3>Código inválido ou expirado.</h3><a href="/verify?email=${encodeURIComponent(email)}">Tentar novamente</a>`);
    }
});

// ==========================================
// ROTAS DA ETAPA 4 (REGISTER E DASHBOARD)
// ==========================================

// Serve a tela de registro
app.get('/register', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'register.html'));
});

// Proxy para Atualizar Perfil (Detetive)
app.post('/register-profile', async (req, res) => {
    const { name, role } = req.body;
    const token = req.headers['authorization'];
    
    console.log("-> Token que o Node recebeu:", token);

    try {
        const respostaJava = await axios.post('http://localhost:8081/users/update-profile', 
            { name, role }, 
            { headers: { Authorization: token } }
        );
        console.log("-> Sucesso no Java!", respostaJava.data);
        res.json({ success: true });
    } catch (error) {
        console.error("-> STATUS DO ERRO NO JAVA:", error.response ? error.response.status : "Sem Resposta");
        console.error("-> TEXTO DO ERRO:", error.response ? error.response.data : error.message);
        res.status(500).json({ error: "Erro ao atualizar perfil" });
    }
});

// Serve a tela do Dashboard
app.get('/dashboard', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'dashboard.html'));
});

// Proxy para testar endpoint protegido
app.get('/api/protected', async (req, res) => {
    const token = req.headers['authorization'];
    try {
        const response = await axios.get('http://localhost:8081/users/test/customer', {
            headers: { Authorization: token }
        });
        res.send(response.data);
    } catch (error) {
        res.status(401).send("Não autorizado. Faça login novamente.");
    }
});

// Proxy para pegar o perfil
app.get('/api/me', async (req, res) => {
    const token = req.headers['authorization'];
    try {
        const response = await axios.get('http://localhost:8081/users/me', {
            headers: { Authorization: token }
        });
        res.json(response.data);
    } catch (error) {
        res.status(401).json({ error: "Não autorizado." });
    }
});

app.listen(PORT, () => {
    console.log(`Frontend rodando em http://localhost:${PORT}`);
});