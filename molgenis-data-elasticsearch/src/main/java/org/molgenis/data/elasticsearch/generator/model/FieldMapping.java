package org.molgenis.data.elasticsearch.generator.model;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class FieldMapping
{
	public abstract String getName();

	public abstract MappingType getType();

	public abstract boolean isAnalyzeNGrams();

	@Nullable
	public abstract List<FieldMapping> getNestedFieldMappings();

	public static FieldMapping create(String newName, MappingType newType, boolean newAnalyzeNGrams,
			List<FieldMapping> newNestedFieldMappings)
	{
		return builder().setName(newName)
						.setType(newType)
						.setAnalyzeNGrams(newAnalyzeNGrams)
						.setNestedFieldMappings(newNestedFieldMappings)
						.build();
	}

	public static Builder builder()
	{
		return new AutoValue_FieldMapping.Builder().setAnalyzeNGrams(false);
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder setName(String newName);

		public abstract Builder setType(MappingType newType);

		public abstract Builder setAnalyzeNGrams(boolean newAnalyzeNGrams);

		public abstract Builder setNestedFieldMappings(List<FieldMapping> newNestedFieldMappings);

		public abstract FieldMapping build();
	}
}
