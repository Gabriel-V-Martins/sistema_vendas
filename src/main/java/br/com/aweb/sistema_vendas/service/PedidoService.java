package br.com.aweb.sistema_vendas.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import br.com.aweb.sistema_vendas.model.Pedido;
import br.com.aweb.sistema_vendas.model.Cliente;
import br.com.aweb.sistema_vendas.model.ItemPedido;
import br.com.aweb.sistema_vendas.model.StatusPedido;
import br.com.aweb.sistema_vendas.repository.PedidoRepository;
import br.com.aweb.sistema_vendas.repository.ProdutoRepository;
import jakarta.transaction.Transactional;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository){
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
    }

    // CREATE
    @Transactional
    public Pedido salvar(Pedido pedido){
        return pedidoRepository.save(pedido);
    }

    // READ
    public List<Pedido> listarTodos(){
        return  pedidoRepository.findAll();
    }

    public Optional<Pedido> buscarPorId(Long id){
        return pedidoRepository.findById(id);
    }

    public List<Pedido> buscarPorStatus(StatusPedido status){
        return pedidoRepository.findByStatus(status);
    }

    public List<Pedido> listarPorStatus(StatusPedido status) {
        return buscarPorStatus(status);
    }

    @Transactional
    public Pedido criarPedido(Cliente cliente) {
        if (cliente == null)
            throw new IllegalArgumentException("Cliente é obrigatório.");
        return salvar(new Pedido(cliente));
    }

    @Transactional
    public void adicionarItem(Long pedidoId, Long produtoId, Integer quantidade) {
        var pedido = buscarPorId(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        var produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        verificarPedidoAtivo(pedido);
        if (quantidade == null || quantidade <= 0)
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        if (produto.getQuantidadeEmEstoque() < quantidade)
            throw new IllegalStateException("Estoque insuficiente para o produto: " + produto.getNome());

        var item = new ItemPedido(produto, quantidade);
        item.setPedido(pedido);
        pedido.getItens().add(item);
        produto.setQuantidadeEmEstoque(produto.getQuantidadeEmEstoque() - quantidade);
        calcularValorTotal(pedido);
        pedidoRepository.save(pedido);
        produtoRepository.save(produto);
    }

    @Transactional
    public void removerItem(Long pedidoId, Long itemId) {
        var pedido = buscarPorId(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        verificarPedidoAtivo(pedido);
        var item = pedido.getItens().stream()
                .filter(i -> itemId != null && itemId.equals(i.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado no pedido."));

        var produto = item.getProduto();
        produto.setQuantidadeEmEstoque(produto.getQuantidadeEmEstoque() + item.getQuantidade());
        pedido.getItens().remove(item);
        item.setPedido(null);
        calcularValorTotal(pedido);
        pedidoRepository.save(pedido);
        produtoRepository.save(produto);
    }

    @Transactional
    public void cancelarPedido(Long pedidoId) {
        var pedido = buscarPorId(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        // Impede que um segundo cancelamento devolva o estoque novamente.
        verificarPedidoAtivo(pedido);
        for (var item : pedido.getItens()) {
            var produto = item.getProduto();
            produto.setQuantidadeEmEstoque(produto.getQuantidadeEmEstoque() + item.getQuantidade());
            produtoRepository.save(produto);
        }
        pedido.setStatus(StatusPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }

    private void verificarPedidoAtivo(Pedido pedido) {
        if (pedido.getStatus() != StatusPedido.ATIVO)
            throw new IllegalStateException("Não é possível alterar pedido cancelado.");
    }

    private void calcularValorTotal(Pedido pedido) {
        var total = pedido.getItens().stream()
                .map(item -> item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setValorTotal(total);
    }
}
