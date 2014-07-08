package com.bis.tomcat;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.ServerInfo;
import org.apache.catalina.valves.ErrorReportValve;
import org.apache.tomcat.util.ExceptionUtils;

import java.io.*;
import java.util.Properties;

public class ErrorValve extends ErrorReportValve
{
	public String getHtmlLocation() throws IOException
	{

		String versionString = null;
		Properties mainProperties = new Properties();
		FileInputStream file;

		//the base folder is ./, the root of the main.properties file
		String path = "./conf/tomcat-error-valve.properties";
		file = new FileInputStream(path);
		mainProperties.load(file);
		file.close();
		versionString = mainProperties.getProperty("html.path");
		return versionString;
	}

	public String getHtmlPage(String path) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(path));
		try
		{
			String line = br.readLine();

			while (line != null)
			{
				sb.append(line);
				sb.append(System.getProperty("line.separator"));
				line = br.readLine();
			}
		} finally
		{
			br.close();
		}
		return sb.toString();
	}

	/**
	 * Prints out an error report.
	 *
	 * @param request   The request being processed
	 * @param response  The response being generated
	 * @param throwable The exception that occurred (which possibly wraps
	 *                  a root cause exception
	 */
	protected void report(Request request, Response response,
						  Throwable throwable)
	{

		// Do nothing on non-HTTP responses
		int statusCode = response.getStatus();

		// Do nothing on a 1xx, 2xx and 3xx status
		// Do nothing if anything has been written already
		if ((statusCode < 400) /*|| (response.getContentCount() > 0 - this appears to prevent the valve from executing */)
			return;

		String message = RequestUtil.filter(response.getMessage());
		if (message == null)
			message = "";

		// Do nothing if there is no report for the specified status code
		String report = null;
		try
		{
			report = sm.getString("http." + statusCode, message);
		} catch (Throwable t)
		{
			ExceptionUtils.handleThrowable(t);
		}
		if (report == null)
		{
			// this seems to be null even in valid cases where we want the valve to handle the 404
			//return;
		}

		StringBuilder sb = new StringBuilder();

		try
		{
			String pageString = getHtmlPage(getHtmlLocation());
			sb.append(pageString);
		} catch (Throwable e)
		{
			appendPage(throwable, statusCode, message, report, sb);
		}

		try
		{
			try
			{
				response.setContentType("text/html");
				response.setCharacterEncoding("utf-8");
			} catch (Throwable t)
			{
				ExceptionUtils.handleThrowable(t);
				if (container.getLogger().isDebugEnabled())
					container.getLogger().debug("status.setContentType", t);
			}
			Writer writer = response.getReporter();
			if (writer != null)
			{
				// If writer is null, it's an indication that the response has
				// been hard committed already, which should never happen
				writer.write(sb.toString());
			}
		} catch (IOException e)
		{
			// Ignore
		} catch (IllegalStateException e)
		{
			// Ignore
		}
	}

	private void appendPage(Throwable throwable, int statusCode, String message, String report, StringBuilder sb)
	{
		sb.append("<html><head><title>");
		sb.append(ServerInfo.getServerInfo()).append(" - ");
		sb.append(sm.getString("errorReportValve.errorReport"));
		sb.append("</title>");
		sb.append("<style><!--");
		sb.append(org.apache.catalina.util.TomcatCSS.TOMCAT_CSS);
		sb.append("--></style> ");
		sb.append("</head><body>");
		sb.append("<h1>");
		sb.append(sm.getString("errorReportValve.statusHeader",
				"" + statusCode, message)).append("</h1>");
		sb.append("<HR size=\"1\" noshade=\"noshade\">");
		sb.append("<p><b>type</b> ");
		if (throwable != null)
		{
			sb.append(sm.getString("errorReportValve.exceptionReport"));
		} else
		{
			sb.append(sm.getString("errorReportValve.statusReport"));
		}
		sb.append("</p>");
		sb.append("<p><b>");
		sb.append(sm.getString("errorReportValve.message"));
		sb.append("</b> <u>");
		sb.append(message).append("</u></p>");
		sb.append("<p><b>");
		sb.append(sm.getString("errorReportValve.description"));
		sb.append("</b> <u>");
		sb.append(report);
		sb.append("</u></p>");

		if (throwable != null)
		{

			String stackTrace = getPartialServletStackTrace(throwable);
			sb.append("<p><b>");
			sb.append(sm.getString("errorReportValve.exception"));
			sb.append("</b> <pre>");
			sb.append(RequestUtil.filter(stackTrace));
			sb.append("</pre></p>");

			int loops = 0;
			Throwable rootCause = throwable.getCause();
			while (rootCause != null && (loops < 10))
			{
				stackTrace = getPartialServletStackTrace(rootCause);
				sb.append("<p><b>");
				sb.append(sm.getString("errorReportValve.rootCause"));
				sb.append("</b> <pre>");
				sb.append(RequestUtil.filter(stackTrace));
				sb.append("</pre></p>");
				// In case root cause is somehow heavily nested
				rootCause = rootCause.getCause();
				loops++;
			}

			sb.append("<p><b>");
			sb.append(sm.getString("errorReportValve.note"));
			sb.append("</b> <u>");
			sb.append(sm.getString("errorReportValve.rootCauseInLogs",
					ServerInfo.getServerInfo()));
			sb.append("</u></p>");

		}

		sb.append("<HR size=\"1\" noshade=\"noshade\">");
		sb.append("<h3>").append(ServerInfo.getServerInfo()).append("</h3>");
		sb.append("</body></html>");
	}

}
