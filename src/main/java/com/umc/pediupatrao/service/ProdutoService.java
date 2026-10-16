package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.repository.ProdutoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public Produto novoProduto(Produto produto) {
        return produtoRepository.save(produto);
    }

    public List<Produto> listarProdutos() {
        return produtoRepository.findAll();
    }

    public Optional<Produto> buscarPorId(String id) {
        return produtoRepository.findById(id);
    }

    public void excluir(String id) {
        produtoRepository.deleteById(id);
    }

    public Produto salvar(Produto produto) {
        if (produto.getAtivo() == null) {
            produto.setAtivo(false);
        }
        if (produto.getId() == null) {
            return produtoRepository.save(produto);
        }
        if (produtoRepository.existsById(produto.getId())) {
            return produtoRepository.save(produto);
        }
        throw new IllegalArgumentException("Produto não encontrado para atualização.");
    }
}
