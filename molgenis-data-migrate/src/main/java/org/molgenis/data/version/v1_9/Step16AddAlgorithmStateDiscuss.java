package org.molgenis.data.version.v1_9;

import javax.sql.DataSource;

import org.molgenis.data.version.MolgenisUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.base.Preconditions;

/**
 * Migration for the introduction of attributes attribute "defaultValue".
 * <ol>
 * <li>Creates the column in the database</li>
 * <li>Iterates over the already imported JPA entities to write their defaultValue to the newly created column</li>
 * <li>Sets the defaultValue for the generateToken attribute of the Script entity</li>
 * <li>Recreates the attributes index in elasticsearch to reflect these changes</li>
 * </ol>
 * 
 */
public class Step16AddAlgorithmStateDiscuss extends MolgenisUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step16AddAlgorithmStateDiscuss.class);
	private final JdbcTemplate jdbcTemplate;

	public Step16AddAlgorithmStateDiscuss(DataSource dataSource)
	{
		super(15, 16);
		this.jdbcTemplate = new JdbcTemplate(Preconditions.checkNotNull(dataSource));
	}

	@Override
	public void upgrade()
	{
		LOG.info("Updating metadata from version 15 to 16");
		LOG.info("Updating AttributeMapping --- add DISCUSS enum");
		jdbcTemplate
				.execute("ALTER TABLE `AttributeMapping` CHANGE `algorithmState` `algorithmState` ENUM('CURATED','GENERATED_HIGH','GENERATED_LOW','DISCUSS');");
		LOG.info("Updating attributes --- add DISCUSS enumOptions");
		jdbcTemplate
				.execute("UPDATE `attributes` SET `enumOptions` = 'CURATED,GENERATED_HIGH,GENERATED_LOW,DISCUSS' WHERE `name` = 'algorithmState' AND `dataType` = 'enum' AND `enumOptions` = 'CURATED,GENERATED_HIGH,GENERATED_LOW';");
	}
}
