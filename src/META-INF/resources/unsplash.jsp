<%--
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
--%>

<%@ include file="/init.jsp" %>

<%
PortletURL portletURL = (PortletURL)request.getAttribute("portletURL");
List<UnsplashImage> unsplashImages = (List<UnsplashImage>)request.getAttribute("unsplashImages");
String itemSelectedEventName = (String)request.getAttribute("itemSelectedEventName");
%>

<div id="<portlet:namespace />flickerImageSelectorWrapper">

	<liferay-ui:search-container
		emptyResultsMessage="there-are-no-unsplash-images"
		iteratorURL="<%= portletURL %>"
		total="<%= GetterUtil.getInteger(request.getAttribute("total")) %>"
	>
		<liferay-ui:search-container-results
			results="<%= unsplashImages %>"
		/>

		<liferay-ui:search-container-row
			className="com.liferay.unsplash.item.selector.web.UnsplashImage"
			cssClass="col-md-2 col-sm-4 col-xs-6"
			modelVar="unsplashImage"
		>
			<liferay-ui:search-container-column-text>
				<div class="unsplash-image" data-url="<%= unsplashImage.getURL() %>">
					<liferay-frontend:card
						imageUrl="<%= unsplashImage.getPreviewURL() %>"
					/>
				</div>
			</liferay-ui:search-container-column-text>
		</liferay-ui:search-container-row>

		<liferay-ui:search-iterator displayStyle="icon" markupView="lexicon" paginate="<%= false %>" />

		<liferay-ui:search-paginator searchContainer="<%= searchContainer %>" />
	</liferay-ui:search-container>
</div>

<aui:script use="flickr-item-selector">
	new Liferay.FlickrItemSelector(
		{
			closeCaption: 'unsplash',
			namespace: '<portlet:namespace/>',
			on: {
				selectedItem: function(event) {
					Liferay.Util.getOpener().Liferay.fire('<%= itemSelectedEventName %>', event);
				}
			}
		}
	);
</aui:script>