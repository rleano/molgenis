package org.molgenis.integrationtest.utils.config;

import org.molgenis.core.ui.MolgenisWebAppConfig;
import org.molgenis.core.ui.util.GsonConfig;
import org.molgenis.integrationtest.data.settings.SettingsTestConfig;
import org.molgenis.integrationtest.ui.ViewTestConfig;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ GsonConfig.class, ViewTestConfig.class, SettingsTestConfig.class })
public class WebAppITConfig extends MolgenisWebAppConfig
{
	@Bean
	public ApplicationContextProvider applicationContextProvider()
	{
		return new ApplicationContextProvider();
	}
}
