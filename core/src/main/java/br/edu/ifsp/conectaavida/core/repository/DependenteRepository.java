package br.edu.ifsp.conectaavida.core.repository;

import br.edu.ifsp.conectaavida.core.domain.Dependente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DependenteRepository extends JpaRepository<Dependente, Long> {

    List<Dependente> findByUsuarioTitularIdAndAtivoTrueOrderByNomeCompletoAsc(Long usuarioTitularId);

    Optional<Dependente> findByIdAndUsuarioTitularIdAndAtivoTrue(Long id, Long usuarioTitularId);

    Optional<Dependente> findByIdAndUsuarioTitularId(Long id, Long usuarioTitularId);
}
