package br.com.aweb.sistema_vendas.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "produtos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "Preço é obrigatória")
    @Column(nullable = false, length = 255)
    private String descricao;

    @NotNull(message = "Preço é obrigatório")
    @PositiveOrZero(message = "O valor deve ser maior ou igual a zero")
    @Column(nullable = false)
    private BigDecimal preco;

    @NotNull(message = "Quantidade é obrigatória")
    @PositiveOrZero(message = "A quantidade deve ser maior ou igual a zero")
    private Integer quantidadeEmEstoque;

}
