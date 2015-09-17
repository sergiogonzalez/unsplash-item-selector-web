/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.unsplash.item.selector.web;

import aQute.bnd.annotation.metatype.Configurable;

import com.liferay.item.selector.ItemSelectorReturnType;
import com.liferay.item.selector.ItemSelectorView;
import com.liferay.item.selector.criteria.URLItemSelectorReturnType;
import com.liferay.item.selector.criteria.image.criterion.ImageItemSelectorCriterion;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.unsplash.item.selector.web.configuration.UnsplashItemSelectorConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.portlet.PortletURL;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Sergio González
 */
@Component(
	configurationPid = "com.liferay.unsplash.item.selector.web.configuration.UnsplashItemSelectorConfiguration",
	immediate = true, service = ItemSelectorView.class
)
public class UnsplashItemSelectorView
	implements ItemSelectorView<ImageItemSelectorCriterion> {

	@Override
	public Class<ImageItemSelectorCriterion> getItemSelectorCriterionClass() {
		return ImageItemSelectorCriterion.class;
	}

	public ServletContext getServletContext() {
		return _servletContext;
	}

	@Override
	public List<ItemSelectorReturnType> getSupportedItemSelectorReturnTypes() {
		return _supportedItemSelectorReturnTypes;
	}

	@Override
	public String getTitle(Locale locale) {
		return getLanguageKey(locale, "unsplash");
	}

	@Override
	public boolean isShowSearch() {
		return true;
	}

	@Override
	public boolean isVisible(ThemeDisplay themeDisplay) {
		if (Validator.isNull(
				_unsplashItemSelectorConfiguration.applicationId())) {

			_log.error("Please configure Unsplash Application ID");

			return false;
		}

		return true;
	}

	@Override
	public void renderHTML(
			ServletRequest request, ServletResponse response,
			ImageItemSelectorCriterion imageItemSelectorCriterion,
			PortletURL portletURL, String itemSelectedEventName, boolean search)
		throws IOException, ServletException {

		String url = null;

		if (search) {
			url = getSearchURL(request);
		}
		else {
			url = getPhotosURL(request);
		}

		URL urlObject = new URL(url);

		HttpURLConnection httpURLConnection =
			(HttpURLConnection)urlObject.openConnection();

		httpURLConnection.setRequestMethod("GET");
		httpURLConnection.setRequestProperty("Accept-Version", "v1");

		int responseCode = httpURLConnection.getResponseCode();
		int total = GetterUtil.getInteger(
			httpURLConnection.getHeaderField("X-Total"));

		List<UnsplashImage> unsplashImages = new ArrayList<>();

		if (responseCode != HttpURLConnection.HTTP_OK) {
			_log.error("Unsplash return a response Code " + responseCode);

			return;
		}

		InputStream inputStream = (InputStream)httpURLConnection.getContent();

		try {
			JSONArray jsonArray = getJSONArrayResponse(inputStream);

			for (int i = 0; i < jsonArray.length(); i++) {
				UnsplashImage unsplashImage = getUnsplashImage(
					jsonArray.getJSONObject(i));

				unsplashImages.add(unsplashImage);
			}
		}
		catch (JSONException e) {
			_log.error("Cannot read JSON Response");
		}

		request.setAttribute("total", total);
		request.setAttribute("unsplashImages", unsplashImages);
		request.setAttribute("portletURL", portletURL);
		request.setAttribute("itemSelectedEventName", itemSelectedEventName);

		ServletContext servletContext = getServletContext();

		RequestDispatcher requestDispatcher =
			servletContext.getRequestDispatcher("/unsplash.jsp");

		requestDispatcher.include(request, response);
	}

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.unsplash.item.selector.web)",
		unbind = "-"
	)
	public void setServletContext(ServletContext servletContext) {
		_servletContext = servletContext;
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_unsplashItemSelectorConfiguration = Configurable.createConfigurable(
			UnsplashItemSelectorConfiguration.class, properties);
	}

	protected JSONArray getJSONArrayResponse(InputStream inputStream)
		throws IOException, JSONException {

		BufferedReader bufferedReader = new BufferedReader(
			new InputStreamReader(inputStream));

		StringBundler sb = new StringBundler();

		String line = null;

		while ((line = bufferedReader.readLine()) != null) {
			sb.append(line);
		}

		return JSONFactoryUtil.createJSONArray(sb.toString());
	}

	protected String getLanguageKey(Locale locale, String key) {
		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
			"content/Language", locale, getClass());

		return resourceBundle.getString(key);
	}

	protected String getPhotosURL(ServletRequest request) {
		int delta = GetterUtil.getInteger(
			request.getParameter(SearchContainer.DEFAULT_DELTA_PARAM),
			SearchContainer.DEFAULT_DELTA);
		int cur = GetterUtil.getInteger(
			request.getParameter(SearchContainer.DEFAULT_CUR_PARAM),
			SearchContainer.DEFAULT_CUR);

		StringBundler sb = new StringBundler(7);

		sb.append("https://api.unsplash.com/photos/?");
		sb.append("client_id=");
		sb.append(_unsplashItemSelectorConfiguration.applicationId());
		sb.append("&page=");
		sb.append(cur);
		sb.append("&per_page=");
		sb.append(delta);

		return sb.toString();
	}

	protected String getSearchURL(ServletRequest request) {
		int delta = GetterUtil.getInteger(
			request.getParameter(SearchContainer.DEFAULT_DELTA_PARAM),
			SearchContainer.DEFAULT_DELTA);
		int cur = GetterUtil.getInteger(
			request.getParameter(SearchContainer.DEFAULT_CUR_PARAM),
			SearchContainer.DEFAULT_CUR);

		String keywords = GetterUtil.getString(
			request.getParameter("keywords"));

		StringBundler sb = new StringBundler(9);

		sb.append("https://api.unsplash.com/photos/search/?");
		sb.append("query=");
		sb.append(HtmlUtil.escape(keywords));
		sb.append("&client_id=");
		sb.append(_unsplashItemSelectorConfiguration.applicationId());
		sb.append("&page=");
		sb.append(cur);
		sb.append("&per_page=");
		sb.append(delta);

		return sb.toString();
	}

	protected UnsplashImage getUnsplashImage(JSONObject jsonObject) {
		JSONObject userJsonObject = jsonObject.getJSONObject("user");

		String name = userJsonObject.getString("name");

		JSONObject links = jsonObject.getJSONObject("links");

		String imageUrl = links.getString("download");

		JSONObject urls = jsonObject.getJSONObject("urls");

		String previewURL = urls.getString("small");

		return new UnsplashImage(imageUrl, name, previewURL);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		UnsplashItemSelectorView.class);

	private static final List<ItemSelectorReturnType>
		_supportedItemSelectorReturnTypes = Collections.unmodifiableList(
			ListUtil.fromArray(
				new ItemSelectorReturnType[] {
					new URLItemSelectorReturnType()
				}));

	private ServletContext _servletContext;
	private volatile UnsplashItemSelectorConfiguration
		_unsplashItemSelectorConfiguration;

}