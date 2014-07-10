package com.bis.tomcat;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class ErrorValveTest
{
	private Request request;
	private Response response;
	private Throwable throwable;
	private PrintWriter mockWriter;

	final String expectedHtmlPage = "start test content\n" +
			"end test content\n";

	@Before
	public void setUpMocks() throws IOException
	{
		request = mock(Request.class);
		response = mock(Response.class);
		mockWriter = mock(PrintWriter.class);
		given(response.getReporter()).willReturn(mockWriter);
		//throwable = mock(Throwable.class);
		try
		{
			String str = null;
			str.toString();
		}
		catch (Exception e)
		{
			throwable = e;
		}
	}

	@Test
	public void testShouldLoadStringFromFile() throws IOException
	{
		ErrorValve errorValve = new ErrorValve();
		String filePath = getClass().getResource("/test.txt").getFile();
		String fileString  = errorValve.getHtmlPage(filePath);

		assertEquals(expectedHtmlPage, fileString);
	}

	@Test
	public void testShouldGetHtmlLocationFromProperties() throws IOException
	{
		ErrorValve errorValve = new ErrorValve();
		String filePath = getClass().getResource("/test.txt").getFile();
		String htmlLocation  = errorValve.getHtmlLocation(getClass().getResource("/test.properties").getFile());
		assertEquals("src/test/resources/test.txt", htmlLocation);
	}

	@Test
	public void testShouldIgnore30X() throws IOException
	{
		given(response.getStatus()).willReturn(302);

		ErrorValve errorValve = new ErrorValve();
		errorValve.report(request, response, throwable);
		verify(response, times(0)).getReporter();
	}

	@Test
	public void testShouldIgnore20X() throws IOException
	{
		given(response.getStatus()).willReturn(200);

		ErrorValve errorValve = new ErrorValve();
		errorValve.report(request, response, throwable);
		verify(response, times(0)).getReporter();
	}

	@Test
	public void testShouldHandle404() throws IOException
	{
		given(response.getStatus()).willReturn(404);

		ErrorValve errorValve = new ErrorValve();
		errorValve.report(request, response, throwable);
		verify(response, times(1)).getReporter();
	}

	@Test
	public void testShouldUseStandardMechanismWhenUnableToLoadProperties() throws IOException
	{
		given(response.getStatus()).willReturn(404);
		ArgumentCaptor<String> pageText = ArgumentCaptor.forClass(String.class);

		ErrorValve errorValve = new ErrorValve();
		errorValve.report(request, response, throwable);

		verify(mockWriter).write(pageText.capture());
		assertTrue("should contain hard coded text", pageText.getValue().contains("<html><head><title>"));
	}

	@Test
	public void testShouldUseVersionFromProperties() throws IOException
	{
		given(response.getStatus()).willReturn(404);
		ArgumentCaptor<String> pageText = ArgumentCaptor.forClass(String.class);

		ErrorValve errorValve = new ErrorValve();
		errorValve.PROPERTIES_PATH = getClass().getResource("/test.properties").getFile();
		errorValve.report(request, response, throwable);

		verify(mockWriter).write(pageText.capture());
		assertTrue("should contain expectedHtmlPage", pageText.getValue().contains(expectedHtmlPage));
	}

}
