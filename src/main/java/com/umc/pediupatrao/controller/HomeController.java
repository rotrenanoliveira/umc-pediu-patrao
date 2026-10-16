package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.dto.HorariosForm;
import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.entity.Entregador;
import com.umc.pediupatrao.entity.Funcionario;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.service.ClienteService;
import com.umc.pediupatrao.service.EntregadorService;
import com.umc.pediupatrao.service.FuncionarioService;
import com.umc.pediupatrao.service.HorarioAtendimentoService;
import com.umc.pediupatrao.service.PedidoService;
import com.umc.pediupatrao.service.ProdutoService;
import com.umc.pediupatrao.service.UsuarioService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private FuncionarioService funcionarioService;

    @Autowired
    private EntregadorService entregadorService;

    @Autowired
    private HorarioAtendimentoService horarioService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("content", "home :: content");
        return "layout";
    }

    // ========================
    // USUÁRIOS
    // ========================
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = (List<Usuario>) usuarioService.listarTodos();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("content", "usuarios/lista :: content");
        log.info("Carregando fragmento: usuarios/lista :: content");
        return "layout";
    }

    @GetMapping("/usuarios/novo")
    public String novoUsuarioForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("content", "usuarios/form :: content");
        return "layout";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String editarUsuarioForm(@PathVariable String id, Model model,
            RedirectAttributes redirectAttributes) {
        boolean encontrado = usuarioService.buscarPorId(id).map(u -> {
            model.addAttribute("usuario", u);
            return true;
        }).orElse(false);

        if (!encontrado) {
            redirectAttributes.addFlashAttribute("erro", "Usuário não encontrado.");
            return "redirect:/usuarios";
        }

        model.addAttribute("content", "usuarios/form :: content");
        return "layout";
    }

    @PostMapping("/usuarios/salvar")
    public String salvarUsuario(@ModelAttribute Usuario usuario,
            RedirectAttributes redirectAttributes) {
        usuarioService.salvarUsuario(usuario);
        redirectAttributes.addFlashAttribute("sucesso", "Usuário criado com sucesso!");
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/editar/{id}")
    public String atualizarUsuario(@PathVariable String id,
            @ModelAttribute Usuario usuario,
            RedirectAttributes redirectAttributes) {
        usuarioService.atualizarUsuario(id, usuario);
        redirectAttributes.addFlashAttribute("sucesso", "Usuário atualizado com sucesso!");
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/deletar/{id}")
    public String deletarUsuario(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        usuarioService.deletarUsuario(id);
        redirectAttributes.addFlashAttribute("sucesso", "Usuário removido com sucesso!");
        return "redirect:/usuarios";
    }

    // ========================
    // PEDIDOS
    // ========================
    @GetMapping("/pedidos")
    public String pedidos(Model model) {
        List<Pedido> pedidos = pedidoService.listarPedidos();
        List<Cliente> clientes = clienteService.listarClientes();
        List<Produto> produtos = produtoService.listarProdutos().stream()
                .filter(p -> Boolean.TRUE.equals(p.getAtivo()))
                .toList();
        Map<String, String> clientesNomes = new HashMap<>();
        clientes.forEach(c -> clientesNomes.put(c.getId(), c.getNome() != null ? c.getNome() : c.getId()));

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("clientes", clientes);
        model.addAttribute("clientesNomes", clientesNomes);
        model.addAttribute("produtos", produtos);
        model.addAttribute("content", "pedidos :: content");
        log.info("Carregando fragmento: pedidos :: content");
        return "layout";
    }

    @GetMapping("/auditoria")
    public String auditoria(Model model) {
        model.addAttribute("content", "auditoria :: content");
        return "layout";
    }

    // ========================
    // PRODUTOS
    // ========================
    @GetMapping("/produtos")
    public String produtos(Model model) {
        List<Produto> produtos = produtoService.listarProdutos();
        model.addAttribute("produtos", produtos);
        model.addAttribute("content", "produtos/lista :: content");
        log.info("Carregando fragmento: produtos/lista :: content");
        return "layout";
    }

    @GetMapping("/produtos/novo")
    public String novoProdutoForm(Model model) {
        Produto produto = new Produto();
        produto.setAtivo(true);
        model.addAttribute("produto", produto);
        model.addAttribute("content", "produtos/form :: content");
        return "layout";
    }

    @GetMapping("/produtos/editar/{id}")
    public String editarProdutoForm(@PathVariable String id, Model model,
            RedirectAttributes redirectAttributes) {
        boolean encontrado = produtoService.buscarPorId(id).map(p -> {
            model.addAttribute("produto", p);
            return true;
        }).orElse(false);

        if (!encontrado) {
            redirectAttributes.addFlashAttribute("erro", "Produto não encontrado.");
            return "redirect:/produtos";
        }

        model.addAttribute("content", "produtos/form :: content");
        return "layout";
    }

    @PostMapping("/produtos/salvar")
    public String salvarProduto(@ModelAttribute Produto produto,
            RedirectAttributes redirectAttributes) {
        if (produto.getAtivo() == null) {
            produto.setAtivo(false);
        }
        produtoService.salvar(produto);
        redirectAttributes.addFlashAttribute("sucesso", "Produto criado com sucesso!");
        return "redirect:/produtos";
    }

    @PostMapping("/produtos/editar/{id}")
    public String atualizarProduto(@PathVariable String id,
            @ModelAttribute Produto produto,
            RedirectAttributes redirectAttributes) {
        produto.setId(id);
        if (produto.getAtivo() == null) {
            produto.setAtivo(false);
        }
        produtoService.salvar(produto);
        redirectAttributes.addFlashAttribute("sucesso", "Produto atualizado com sucesso!");
        return "redirect:/produtos";
    }

    @PostMapping("/produtos/deletar/{id}")
    public String deletarProduto(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        produtoService.excluir(id);
        redirectAttributes.addFlashAttribute("sucesso", "Produto removido com sucesso!");
        return "redirect:/produtos";
    }

    // ========================
    // CLIENTES
    // ========================
    @GetMapping("/clientes")
    public String clientes(Model model) {
        List<Cliente> clientes = clienteService.listarClientes();
        model.addAttribute("clientes", clientes);
        model.addAttribute("content", "clientes/lista :: content");
        log.info("Carregando fragmento: clientes/lista :: content");
        return "layout";
    }

    @GetMapping("/clientes/novo")
    public String novoClienteForm(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("content", "clientes/form :: content");
        return "layout";
    }

    @PostMapping("/clientes/salvar")
    public String salvarCliente(@ModelAttribute Cliente cliente,
            RedirectAttributes redirectAttributes) {
        clienteService.salvar(cliente);
        redirectAttributes.addFlashAttribute("sucesso", "Cliente criado com sucesso!");
        return "redirect:/clientes";
    }

    @GetMapping("/clientes/editar/{id}")
    public String editarClienteForm(@PathVariable String id, Model model,
            RedirectAttributes redirectAttributes) {
        boolean encontrado = clienteService.buscarPorId(id).map(c -> {
            model.addAttribute("cliente", c);
            return true;
        }).orElse(false);

        if (!encontrado) {
            redirectAttributes.addFlashAttribute("erro", "Cliente não encontrado.");
            return "redirect:/clientes";
        }

        model.addAttribute("content", "clientes/form :: content");
        return "layout";
    }

    @PostMapping("/clientes/editar/{id}")
    public String atualizarCliente(@PathVariable String id,
            @ModelAttribute Cliente cliente,
            RedirectAttributes redirectAttributes) {
        cliente.setId(id);
        clienteService.salvar(cliente);
        redirectAttributes.addFlashAttribute("sucesso", "Cliente atualizado com sucesso!");
        return "redirect:/clientes";
    }

    @PostMapping("/clientes/deletar/{id}")
    public String deletarCliente(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        clienteService.excluir(id);
        redirectAttributes.addFlashAttribute("sucesso", "Cliente removido com sucesso!");
        return "redirect:/clientes";
    }

    // ========================
    // FUNCIONÁRIOS
    // ========================
    @GetMapping("/funcionarios")
    public String funcionarios(Model model) {
        model.addAttribute("funcionarios", funcionarioService.listar());
        model.addAttribute("content", "funcionarios/lista :: content");
        return "layout";
    }

    @GetMapping("/funcionarios/novo")
    public String novoFuncionarioForm(Model model) {
        Funcionario funcionario = new Funcionario();
        funcionario.setAtivo(true);
        model.addAttribute("funcionario", funcionario);
        model.addAttribute("content", "funcionarios/form :: content");
        return "layout";
    }

    @GetMapping("/funcionarios/editar/{id}")
    public String editarFuncionarioForm(@PathVariable String id, Model model,
            RedirectAttributes redirectAttributes) {
        boolean encontrado = funcionarioService.buscarPorId(id).map(f -> {
            model.addAttribute("funcionario", f);
            return true;
        }).orElse(false);

        if (!encontrado) {
            redirectAttributes.addFlashAttribute("erro", "Funcionário não encontrado.");
            return "redirect:/funcionarios";
        }

        model.addAttribute("content", "funcionarios/form :: content");
        return "layout";
    }

    @PostMapping("/funcionarios/salvar")
    public String salvarFuncionario(@ModelAttribute Funcionario funcionario,
            RedirectAttributes redirectAttributes) {
        if (funcionario.getAtivo() == null) {
            funcionario.setAtivo(false);
        }
        funcionarioService.salvar(funcionario);
        redirectAttributes.addFlashAttribute("sucesso", "Funcionário criado com sucesso!");
        return "redirect:/funcionarios";
    }

    @PostMapping("/funcionarios/editar/{id}")
    public String atualizarFuncionario(@PathVariable String id,
            @ModelAttribute Funcionario funcionario,
            RedirectAttributes redirectAttributes) {
        funcionario.setId(id);
        if (funcionario.getAtivo() == null) {
            funcionario.setAtivo(false);
        }
        funcionarioService.salvar(funcionario);
        redirectAttributes.addFlashAttribute("sucesso", "Funcionário atualizado com sucesso!");
        return "redirect:/funcionarios";
    }

    @PostMapping("/funcionarios/deletar/{id}")
    public String deletarFuncionario(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        funcionarioService.excluir(id);
        redirectAttributes.addFlashAttribute("sucesso", "Funcionário removido com sucesso!");
        return "redirect:/funcionarios";
    }

    // ========================
    // ENTREGADORES
    // ========================
    @GetMapping("/entregadores")
    public String entregadores(Model model) {
        model.addAttribute("entregadores", entregadorService.listar());
        model.addAttribute("content", "entregadores/lista :: content");
        return "layout";
    }

    @GetMapping("/entregadores/novo")
    public String novoEntregadorForm(Model model) {
        Entregador entregador = new Entregador();
        entregador.setAtivo(true);
        model.addAttribute("entregador", entregador);
        model.addAttribute("content", "entregadores/form :: content");
        return "layout";
    }

    @GetMapping("/entregadores/editar/{id}")
    public String editarEntregadorForm(@PathVariable String id, Model model,
            RedirectAttributes redirectAttributes) {
        boolean encontrado = entregadorService.buscarPorId(id).map(e -> {
            model.addAttribute("entregador", e);
            return true;
        }).orElse(false);

        if (!encontrado) {
            redirectAttributes.addFlashAttribute("erro", "Entregador não encontrado.");
            return "redirect:/entregadores";
        }

        model.addAttribute("content", "entregadores/form :: content");
        return "layout";
    }

    @PostMapping("/entregadores/salvar")
    public String salvarEntregador(@ModelAttribute Entregador entregador,
            RedirectAttributes redirectAttributes) {
        if (entregador.getAtivo() == null) {
            entregador.setAtivo(false);
        }
        entregadorService.salvar(entregador);
        redirectAttributes.addFlashAttribute("sucesso", "Entregador criado com sucesso!");
        return "redirect:/entregadores";
    }

    @PostMapping("/entregadores/editar/{id}")
    public String atualizarEntregador(@PathVariable String id,
            @ModelAttribute Entregador entregador,
            RedirectAttributes redirectAttributes) {
        entregador.setId(id);
        if (entregador.getAtivo() == null) {
            entregador.setAtivo(false);
        }
        entregadorService.salvar(entregador);
        redirectAttributes.addFlashAttribute("sucesso", "Entregador atualizado com sucesso!");
        return "redirect:/entregadores";
    }

    @PostMapping("/entregadores/deletar/{id}")
    public String deletarEntregador(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        entregadorService.excluir(id);
        redirectAttributes.addFlashAttribute("sucesso", "Entregador removido com sucesso!");
        return "redirect:/entregadores";
    }

    // ========================
    // HORÁRIOS
    // ========================
    @GetMapping("/horarios")
    public String horarios(Model model) {
        HorariosForm form = new HorariosForm();
        form.setHorarios(horarioService.listarOuInicializar());
        model.addAttribute("form", form);

        Map<String, String> rotulos = new HashMap<>();
        form.getHorarios().forEach(h -> rotulos.put(h.getDiaSemana(), horarioService.rotuloDia(h.getDiaSemana())));
        model.addAttribute("rotulos", rotulos);
        model.addAttribute("content", "horarios/lista :: content");
        return "layout";
    }

    @PostMapping("/horarios/salvar")
    public String salvarHorarios(@ModelAttribute("form") HorariosForm form,
            RedirectAttributes redirectAttributes) {
        if (form.getHorarios() != null) {
            form.getHorarios().forEach(h -> {
                if (h.getFechado() == null) {
                    h.setFechado(false);
                }
            });
            horarioService.salvarTodos(form.getHorarios());
        }
        redirectAttributes.addFlashAttribute("sucesso", "Horários atualizados com sucesso!");
        return "redirect:/horarios";
    }

    // ========================
    // OUTRAS ROTAS
    // ========================
    @GetMapping("/configuracoes")
    public String configuracoes(Model model) {
        model.addAttribute("content", "configuracoes :: content");
        return "layout";
    }

    @GetMapping("/teste")
    public String teste() {
        return "teste";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }
}
