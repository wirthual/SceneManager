package com.adfmanager.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfmanager.domain.AdfDescription;

public class AdfDescriptionMapper implements RowMapper<AdfDescription> {
	public AdfDescription mapRow(ResultSet rs, int rowNum) throws SQLException {
		AdfDescription desc = new AdfDescription();
		desc.setId(rs.getLong("id"));
		desc.setName(rs.getString("name"));
		desc.setFileName(rs.getString("file_name"));
		desc.setUuid(rs.getString("uuid"));
		desc.setDescription(rs.getString("description"));
		desc.setLat(rs.getDouble("lat"));
		desc.setLng(rs.getDouble("lng"));
		desc.setAlt(rs.getDouble("alt"));
		desc.setType(rs.getString("type"));
		desc.setLvl(rs.getInt("lvl"));

		return desc;
	}
}