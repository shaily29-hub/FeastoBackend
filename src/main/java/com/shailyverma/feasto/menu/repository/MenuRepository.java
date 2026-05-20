package com.shailyverma.feasto.menu.repository;

import com.shailyverma.feasto.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MenuRepository extends JpaRepository<Menu, Long >, JpaSpecificationExecutor<Menu> {

}
