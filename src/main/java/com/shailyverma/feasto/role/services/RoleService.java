package com.shailyverma.feasto.role.services;

import com.shailyverma.feasto.response.Response;
import com.shailyverma.feasto.role.dtos.RoleDTO;

import java.util.List;

public interface RoleService {
    Response<RoleDTO> createRole(RoleDTO roleDTO);
    Response<RoleDTO> updateRole(RoleDTO roleDTO);
    Response<List<RoleDTO>> getAllRoles();
    Response<?> deleteRole(Long id);
}
