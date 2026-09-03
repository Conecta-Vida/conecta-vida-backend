package br.edu.ifsp.conectaavida.core.repository;

import br.edu.ifsp.conectaavida.core.domain.LogAtividade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * REPOSITÓRIO DE AUDITORIA E LOGS
 *
 * Explicação para a Equipe:
 * Esta tabela registra tudo o que os administradores fazem (quem postou, quem apagou).
 * É uma exigência de segurança para sabermos quem fez o que no sistema.
 */
@Repository
public interface LogAtividadeRepository extends JpaRepository<LogAtividade, Long> {

    // Traz apenas as 5 últimas ações registradas no sistema, da mais recente para a mais antiga.
    // O "Top5" limita o SELECT no banco (equivalente a LIMIT 5), economizando muita memória!
    List<LogAtividade> findTop5ByOrderByDataHoraDesc();
}