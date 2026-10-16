package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.LinkedHashMap;
import com.umc.pediupatrao.entity.AlteracaoAuditoria;

@Service
public class ClienteService {

    private ClienteRepository clienteRepository;
    private final AuditoriaService auditoriaService;

    public ClienteService(ClienteRepository clienteRepository, AuditoriaService auditoriaService) {
        this.clienteRepository = clienteRepository;
        this.auditoriaService = auditoriaService;
    }
    public Cliente novoCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    // Método para excluir cliente
    public void excluir(String id) {
        clienteRepository.deleteById(id);
    }

    public Optional<Cliente> buscarPorId(String id) {
        return clienteRepository.findById(id);
    }

    // Método para salvar um novo cliente ou atualizar um cliente existente
    public Cliente salvar(Cliente cliente) {
        // Se o cliente não tem ID (novo cliente), salva como novo
        if (cliente.getId() == null) {
            Cliente salvo = clienteRepository.save(cliente);
            auditoriaService.registrar("INCLUSAO_CLIENTE", "CLIENTE", salvo.getId(), new LinkedHashMap<>(), null);
            return salvo;
        } // Se já tem ID (cliente existente), atualiza
        else {
            // Verifica se o cliente existe antes de atualizar
            if (clienteRepository.existsById(cliente.getId())) {
                Cliente anterior = clienteRepository.findById(cliente.getId()).orElseThrow();
                Cliente salvo = clienteRepository.save(cliente);
                LinkedHashMap<String, AlteracaoAuditoria> mudancas = mudancas(anterior, salvo);
                if (!mudancas.isEmpty()) auditoriaService.registrar("ALTERACAO_CLIENTE", "CLIENTE", salvo.getId(), mudancas, null);
                return salvo;
            } else {
                throw new IllegalArgumentException("Cliente não encontrado para atualização.");
            }
        }
    }

    private LinkedHashMap<String, AlteracaoAuditoria> mudancas(Cliente antes, Cliente depois) {
        LinkedHashMap<String, AlteracaoAuditoria> resultado = new LinkedHashMap<>();
        comparar(resultado, "nome", antes.getNome(), depois.getNome());
        comparar(resultado, "telefone", antes.getTelefone(), depois.getTelefone());
        comparar(resultado, "cep", antes.getCep(), depois.getCep());
        comparar(resultado, "logradouro", antes.getLogradouro(), depois.getLogradouro());
        comparar(resultado, "numero", antes.getNumero(), depois.getNumero());
        comparar(resultado, "complemento", antes.getComplemento(), depois.getComplemento());
        comparar(resultado, "bairro", antes.getBairro(), depois.getBairro());
        comparar(resultado, "cidade", antes.getCidade(), depois.getCidade());
        comparar(resultado, "estado", antes.getEstado(), depois.getEstado());
        comparar(resultado, "endereco", antes.getEndereco(), depois.getEndereco());
        return resultado;
    }
    private void comparar(LinkedHashMap<String, AlteracaoAuditoria> resultado, String campo, String anterior, String novo) {
        if (!java.util.Objects.equals(anterior, novo)) resultado.put(campo, new AlteracaoAuditoria(anterior, novo));
    }

}
