package br.com.aweb.sistema_vendas.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockMakers;

import br.com.aweb.sistema_vendas.model.Cliente;
import br.com.aweb.sistema_vendas.model.Pedido;
import br.com.aweb.sistema_vendas.model.Produto;
import br.com.aweb.sistema_vendas.model.StatusPedido;
import br.com.aweb.sistema_vendas.repository.PedidoRepository;
import br.com.aweb.sistema_vendas.repository.ProdutoRepository;

class PedidoServiceTests {
    private PedidoRepository pedidos;
    private ProdutoRepository produtos;
    private PedidoService service;
    private Pedido pedido;
    private Produto produto;

    @BeforeEach
    void preparar() {
        pedidos = mock(PedidoRepository.class, withSettings().mockMaker(MockMakers.PROXY));
        produtos = mock(ProdutoRepository.class, withSettings().mockMaker(MockMakers.PROXY));
        service = new PedidoService(pedidos, produtos);
        pedido = new Pedido(new Cliente());
        pedido.setId(1L);
        produto = new Produto(2L, "Produto", "Descrição", new BigDecimal("12.50"), 10);
        when(pedidos.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtos.findById(2L)).thenReturn(Optional.of(produto));
        when(pedidos.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void criarPedidoAtivoSemItens() {
        var cliente = new Cliente();
        var criado = service.criarPedido(cliente);
        assertSame(cliente, criado.getCliente());
        assertEquals(StatusPedido.ATIVO, criado.getStatus());
        assertNotNull(criado.getDataPedido());
        assertTrue(criado.getItens().isEmpty());
        assertEquals(BigDecimal.ZERO, criado.getValorTotal());
    }

    @Test
    void adicionarItensPreservaPrecoDaVendaECalculaTotal() {
        service.adicionarItem(1L, 2L, 3);
        produto.setPreco(new BigDecimal("20.00"));
        service.adicionarItem(1L, 2L, 2);
        assertEquals(5, produto.getQuantidadeEmEstoque());
        assertEquals(new BigDecimal("77.50"), pedido.getValorTotal());
        assertEquals(new BigDecimal("12.50"), pedido.getItens().get(0).getPrecoUnitario());
        assertSame(pedido, pedido.getItens().get(0).getPedido());
        verify(pedidos, times(2)).save(pedido);
        verify(produtos, times(2)).save(produto);
    }

    @Test
    void rejeitarQuantidadesInvalidasSemAlterarEstoque() {
        for (Integer quantidade : new Integer[] {null, 0, -1})
            assertThrows(IllegalArgumentException.class, () -> service.adicionarItem(1L, 2L, quantidade));
        assertThrows(IllegalStateException.class, () -> service.adicionarItem(1L, 2L, 11));
        assertEquals(10, produto.getQuantidadeEmEstoque());
        assertTrue(pedido.getItens().isEmpty());
        verify(pedidos, never()).save(any());
        verify(produtos, never()).save(any());
    }

    @Test
    void removerItemDevolveEstoqueERecalculaTotal() {
        service.adicionarItem(1L, 2L, 3);
        var item = pedido.getItens().get(0);
        item.setId(5L);
        service.adicionarItem(1L, 2L, 2);
        pedido.getItens().get(1).setId(6L);
        service.removerItem(1L, 5L);
        assertEquals(8, produto.getQuantidadeEmEstoque());
        assertEquals(new BigDecimal("25.00"), pedido.getValorTotal());
        assertNull(item.getPedido());
        service.removerItem(1L, 6L);
        assertEquals(10, produto.getQuantidadeEmEstoque());
        assertEquals(BigDecimal.ZERO, pedido.getValorTotal());
        assertTrue(pedido.getItens().isEmpty());
    }

    @Test
    void rejeitarItemDeOutroPedido() {
        service.adicionarItem(1L, 2L, 3);
        pedido.getItens().get(0).setId(5L);
        assertThrows(IllegalArgumentException.class, () -> service.removerItem(1L, 99L));
        assertEquals(7, produto.getQuantidadeEmEstoque());
        assertEquals(1, pedido.getItens().size());
    }

    @Test
    void cancelarDevolveTodoEstoqueSomenteUmaVez() {
        service.adicionarItem(1L, 2L, 3);
        service.adicionarItem(1L, 2L, 2);
        service.cancelarPedido(1L);
        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
        assertEquals(10, produto.getQuantidadeEmEstoque());
        assertEquals(2, pedido.getItens().size());
        assertEquals(new BigDecimal("62.50"), pedido.getValorTotal());
        assertThrows(IllegalStateException.class, () -> service.cancelarPedido(1L));
        assertThrows(IllegalStateException.class, () -> service.adicionarItem(1L, 2L, 1));
        assertThrows(IllegalStateException.class, () -> service.removerItem(1L, 5L));
        assertEquals(10, produto.getQuantidadeEmEstoque());
    }

    @Test
    void rejeitarRegistrosInexistentes() {
        assertThrows(IllegalArgumentException.class, () -> service.adicionarItem(99L, 2L, 1));
        assertThrows(IllegalArgumentException.class, () -> service.adicionarItem(1L, 99L, 1));
        assertThrows(IllegalArgumentException.class, () -> service.removerItem(99L, 5L));
        assertThrows(IllegalArgumentException.class, () -> service.cancelarPedido(99L));
        verify(pedidos, never()).save(any());
        verify(produtos, never()).save(any());
    }
}
