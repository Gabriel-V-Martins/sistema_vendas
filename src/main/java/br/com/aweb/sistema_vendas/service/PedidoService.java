package br.com.aweb.sistema_vendas.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import br.com.aweb.sistema_vendas.model.Pedido;
import br.com.aweb.sistema_vendas.model.StatusPedido;
import br.com.aweb.sistema_vendas.repository.PedidoRepository;
import jakarta.transaction.Transactional;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository){
        this.pedidoRepository = pedidoRepository;
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
}
