package org.molgenis.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLConnection;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for running a script
 * 
 * Url: /scripts/${scriptname}/run
 * 
 * If the script generates an outputfile that file is streamed to the outputstream else the script output
 */
@Controller
public class ScriptRunnerController
{
	private final SavedScriptRunner savedScriptRunner;
	private final FileStore fileStore;

	@Autowired
	private ScriptRunnerController(SavedScriptRunner savedScriptRunner, FileStore fileStore)
	{
		this.savedScriptRunner = savedScriptRunner;
		this.fileStore = fileStore;
	}

	@RequestMapping("/scripts/{name}/run")
	public void runScript(@PathVariable("name") String scriptName, @RequestParam Map<String, Object> parameters,
			HttpServletResponse response) throws IOException
	{
		ScriptResult result = savedScriptRunner.runScript(scriptName, parameters);

		if (result.getOutputFile() != null)
		{
			File f = new File(result.getOutputFile());
			if (f.exists())
			{
				String guessedContentType = URLConnection.guessContentTypeFromName(f.getName());
				if (guessedContentType != null)
				{
					response.setContentType(guessedContentType);
				}

				FileCopyUtils.copy(new FileInputStream(f), response.getOutputStream());
				f.delete();
			}
		}
		else if (StringUtils.isNotBlank(result.getOutput()))
		{
			response.setContentType("text/plain");

			PrintWriter pw = response.getWriter();
			pw.write(result.getOutput());
			pw.flush();
		}
	}

	@ExceptionHandler(GenerateScriptException.class)
	public void handleGenerateScriptException(GenerateScriptException e, HttpServletResponse response)
			throws IOException
	{
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
	}
}
