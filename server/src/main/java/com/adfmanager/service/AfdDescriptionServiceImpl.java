package com.adfmanager.service;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.adfmanager.AdfManagerApplication;
import com.adfmanager.domain.AdfDescription;
import com.adfmanager.repository.AdfDescriptionRepository;
import com.adfmanager.service.exception.AdfDescriptionAlreadyExistsException;

@Service
@Validated
public class AfdDescriptionServiceImpl implements AdfDescriptionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AdfDescriptionService.class);
	private final AdfDescriptionRepository repository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Inject
	public AfdDescriptionServiceImpl(final AdfDescriptionRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<AdfDescription> getList() {
		LOGGER.debug("Retrieving the list of all users");
		return repository.findAll();
	}
	
	public List<AdfDescription> getListByUser(String userId) {
		String sql = "SELECT * FROM adf_description WHERE adf_description.user_id SIMILAR TO '"+userId+"'";

		List<AdfDescription> rows = jdbcTemplate.query(sql, new AdfDescriptionMapper());

		return rows;
	}

	@Override
	public AdfDescription save(@NotNull @Valid AdfDescription desc) {

		AdfDescription existing = repository.findOne(desc.getId());
		if (existing != null) {
			throw new AdfDescriptionAlreadyExistsException(
					String.format("There already exists a user with id=%s", desc.getId()));
		}
		return repository.save(desc);
	}

	@Override
	public AdfDescription getAdfFile(@NotNull @Valid long id) {
		AdfDescription existing = repository.findOne(id);
		if (existing == null) {
			throw new AdfDescriptionAlreadyExistsException(String.format("No descriotion found with id=%s", id));
		}
		return existing;
	}

	@Override
	public List<AdfDescription> getNearbyList(double lng, double lat, double radius, Integer lvl) {
		String lvlClause = "";
		if (null!=lvl) {
			lvlClause = "AND lvl=" + String.valueOf(lvl);
		}

		String sql = " SELECT * FROM adf_description WHERE round((point(lng,lat)<@>point(?,?))::numeric, 3)*1.6 < ?"
				+ lvlClause;

		Object[] args = new Object[] { lng, lat, radius };

		List<AdfDescription> rows = jdbcTemplate.query(sql, args, new AdfDescriptionMapper());

		return rows;
	}

	@Override
	public List<AdfDescription> getListContains(String s) {
		// String sql = "SELECT * FROM adf_description WHERE INSTR(description,
		// '" + s + "') > 0";
		String sql = "SELECT * FROM adf_description WHERE description ILIKE '%" + s + "%'";

		List<AdfDescription> rows = jdbcTemplate.query(sql, new AdfDescriptionMapper());

		return rows;
	}

	@Override
	public boolean delete(long id) {
		AdfDescription existing = repository.findOne(id);
		if (existing == null) {
			return false;
		} else {
			String fileName = existing.getFileName();
			String type = existing.getType();
			try {

				File file = new File(AdfManagerApplication.STORAGE_ROOT+"/"+type, fileName);

				if (file.delete()) {
					repository.delete(existing);
					System.out.println(file.getName() + " is deleted!");
					return true;
				} else {
					System.out.println("Delete operation is failed.");
					return false;
				}

			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}
	

}
