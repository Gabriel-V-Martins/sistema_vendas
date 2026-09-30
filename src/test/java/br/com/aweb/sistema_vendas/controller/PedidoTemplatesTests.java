package br.com.aweb.sistema_vendas.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import br.com.aweb.sistema_vendas.model.Cliente;
import br.com.aweb.sistema_vendas.model.ItemPedido;
import br.com.aweb.sistema_vendas.model.Pedido;
import br.com.aweb.sistema_vendas.model.Produto;
import br.com.aweb.sistema_vendas.model.StatusPedido;

class PedidoTemplatesTests {
    @Test
    void renderizarTelasComPedidoAtivoCanceladoEListasVazias() {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        var servletContext = new MockServletContext();
        var request = new MockHttpServletRequest(servletContext);
        var response = new MockHttpServletResponse();
        var application = JakartaServletWebApplication.buildApplication(servletContext);
        var context = new WebContext(application.buildExchange(request, response));
        var cliente = new Cliente();
        cliente.setNome("Cliente de Teste");
        cliente.setId(1L);
        var produto = new Produto(2L, "Produto de Teste", "Descrição", new BigDecimal("12.50"), 7);
        var pedido = new Pedido(cliente);
        pedido.setId(3L);
        var item = new ItemPedido(produto, 3);
        item.setId(4L);
        item.setPedido(pedido);
        pedido.getItens().add(item);
        pedido.setValorTotal(new BigDecimal("37.50"));
        context.setVariable("pedido", pedido);
        context.setVariable("pedidos", List.of(pedido));
        context.setVariable("clientes", List.of(cliente));
        context.setVariable("produtos", List.of(produto));
        context.setVariable("status", StatusPedido.ATIVO);
        context.setVariable("erro", "Estoque insuficiente.");

        for (var template : List.of("form", "list", "edit", "detalhes", "cancelar")) {
            var html = engine.process("pedido/" + template, context);
            assertTrue(html.contains("Cliente de Teste"), template);
            if (template.equals("edit") || template.equals("detalhes"))
                assertTrue(html.contains("37,50"), template);
        }
        pedido.setStatus(StatusPedido.CANCELADO);
        assertTrue(engine.process("pedido/cancelar", context).contains("Este pedido já está cancelado."));
        context.setVariable("pedidos", List.of());
        context.setVariable("clientes", List.of());
        assertTrue(engine.process("pedido/list", context).contains("Nenhum pedido encontrado."));
        assertTrue(engine.process("pedido/form", context).contains("Cadastre um cliente"));
    }
}
