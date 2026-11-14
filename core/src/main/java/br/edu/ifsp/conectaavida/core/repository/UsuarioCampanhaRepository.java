package br.edu.ifsp.conectaavida.core.repository;

import br.edu.ifsp.conectaavida.core.domain.UsuarioCampanha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioCampanhaRepository extends JpaRepository<UsuarioCampanha, Long> {

    List<UsuarioCampanha> findByUsuarioId(Long usuarioId);

    List<UsuarioCampanha> findByComunicacaoId(Long comunicacaoId);

    List<UsuarioCampanha> findByUsuarioIdAndComunicacaoId(Long usuarioId, Long comunicacaoId);

    Optional<UsuarioCampanha> findByUsuarioIdAndComunicacaoIdAndDependenteIsNull(
            Long usuarioId, Long comunicacaoId);

    Optional<UsuarioCampanha> findByUsuarioIdAndComunicacaoIdAndDependente_Id(
            Long usuarioId, Long comunicacaoId, Long dependenteId);

    @Query("SELECT uc FROM UsuarioCampanha uc WHERE uc.comunicacao.id = :comunicacaoId AND uc.dataNotificacaoFim IS NULL")
    List<UsuarioCampanha> findNaoNotificadasPorComunicacao(@Param("comunicacaoId") Long comunicacaoId);

    long countByComunicacaoId(Long comunicacaoId);

    void deleteByUsuarioId(Long usuarioId);

    void deleteByComunicacaoId(Long comunicacaoId);
}
