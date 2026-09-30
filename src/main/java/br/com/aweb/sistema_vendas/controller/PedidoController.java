package br.com.aweb.sistema_vendas.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.aweb.sistema_vendas.model.Pedido;
import br.com.aweb.sistema_vendas.model.StatusPedido;
import br.com.aweb.sistema_vendas.service.ClienteService;
import br.com.aweb.sistema_vendas.service.PedidoService;
import br.com.aweb.sistema_vendas.service.ProdutoService;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {
    private final PedidoService pedidoService;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;

    public PedidoController(PedidoService pedidoService, ClienteService clienteService,
            ProdutoService produtoService) {
        this.pedidoService = pedidoService;
        this.clienteService = clienteService;
        this.produtoService = produtoService;
    }

    @GetMapping
    public ModelAndView listarPedidos(@RequestParam(required = false) StatusPedido status) {
        var pedidos = status == null ? pedidoService.listarTodos() : pedidoService.listarPorStatus(status);
        var view = new ModelAndView("pedido/list", Map.of("pedidos", pedidos));
        view.addObject("status", status);
        return view;
    }

    @GetMapping("/novo")
    public ModelAndView novoPedidoForm() {
        return new ModelAndView("pedido/form", Map.of("pedido", new Pedido(),
                "clientes", clienteService.listarTodos(), "produtos", produtoService.listarTodos()));
    }

    @PostMapping("/novo")
    public String criarPedido(@RequestParam Long clienteId) {
        var cliente = clienteService.buscarPorId(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado."));
        var pedido = pedidoService.criarPedido(cliente);
        return "redirect:/pedidos/edit/" + pedido.getId();
    }

    @GetMapping("/edit/{id}")
    public ModelAndView editarPedidoForm(@PathVariable Long id) {
        var pedido = obterPedido(id);
        if (pedido.getStatus() == StatusPedido.CANCELADO)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pedido cancelado não pode ser editado.");
        return new ModelAndView("pedido/edit", Map.of("pedido", pedido,
                "produtos", produtoService.listarTodos()));
    }

    @PostMapping("/{pedidoId}/adicionar-item")
    public String adicionarItem(@PathVariable Long pedidoId, @RequestParam Long produtoId,
            @RequestParam Integer quantidade, RedirectAttributes attributes) {
        obterPedido(pedidoId);
        try {
            pedidoService.adicionarItem(pedidoId, produtoId, quantidade);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            attributes.addFlashAttribute("erro", exception.getMessage());
        }
        return "redirect:/pedidos/edit/" + pedidoId;
    }

    @PostMapping("/{pedidoId}/remover-item/{itemId}")
    public String removerItem(@PathVariable Long pedidoId, @PathVariable Long itemId,
            RedirectAttributes attributes) {
        obterPedido(pedidoId);
        try {
            pedidoService.removerItem(pedidoId, itemId);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            attributes.addFlashAttribute("erro", exception.getMessage());
        }
        return "redirect:/pedidos/edit/" + pedidoId;
    }

    @PostMapping("/{id}/finalizar")
    public String finalizarPedido(@PathVariable Long id) {
        obterPedido(id);
        return "redirect:/pedidos";
    }

    @GetMapping("/cancelar/{id}")
    public ModelAndView cancelarPedidoForm(@PathVariable Long id) {
        return new ModelAndView("pedido/cancelar", Map.of("pedido", obterPedido(id)));
    }

    @PostMapping("/cancelar/{id}")
    public String cancelarPedido(@PathVariable Long id, RedirectAttributes attributes) {
        obterPedido(id);
        try {
            pedidoService.cancelarPedido(id);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            attributes.addFlashAttribute("erro", exception.getMessage());
        }
        return "redirect:/pedidos";
    }

    @GetMapping("/detalhes/{id}")
    public ModelAndView detalhesPedido(@PathVariable Long id) {
        return new ModelAndView("pedido/detalhes", Map.of("pedido", obterPedido(id)));
    }

    private Pedido obterPedido(Long id) {
        return pedidoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));
    }
}
