package br.com.aweb.sistema_vendas.model;

import org.hibernate.validator.constraints.br.CPF;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Column(nullable = false, length = 100)
    @Email(message = "Email tem que ser válido")
    private String email;

    @NotBlank(message = "CPF é obrigatório")
    @Column(nullable = false, length = 50)
    @CPF(message = "CPF tem que ser válido")
    private String cpf;

    @NotBlank(message = "Telefone é obrigatório")
    @Column(length = 50)
    private String telefone;

    @NotBlank(message = "Logradouro é obrigatório")
    @Column(length = 100)
    private String logradouro;

    @Column(length = 100)
    private Integer numero;

    @Column(length = 255)
    private String complemento;

    @NotBlank(message = "Bairro é obrigatório")
    @Column(length = 100)
    private String bairro;

    @NotBlank(message = "Cidade é obrigatório")
    @Column(length = 100)
    private String cidade;

    @NotBlank(message = "UF é obrigatório")
    @Column(length = 100)
    private String uf;

    @NotBlank(message = "CEP é obrigatório")
    @Column(length = 100)
    private String cep;

}
