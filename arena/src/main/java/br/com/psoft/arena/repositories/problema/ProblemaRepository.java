package br.com.psoft.arena.repositories.problema;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.psoft.arena.models.problema.ProblemaModel;

public interface ProblemaRepository extends JpaRepository<ProblemaModel, Long> {

}
