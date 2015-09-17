# Unsplash Item Selector Web

This project is an OSGi plugin that extends Liferay Item Selector feature by including a view fetching images from Unsplash (unsplash.com) that will allow portal users to select images from Unsplash when they are creating content, such as Blog Entries, Web Content, and potentially any other portlet that uses Item Selector portlet.

The view will allow the users to browse among Unsplash images and it also supports search by keywords to find relevant pictures or images.

The module requires some minimum configuration in order to work with Unsplash  Web Services. This includes creating a Unsplash account and providing the following Unsplash API information that can be obtained in https://unsplash.com/developers:

* Unsplash Application ID: first register as a developer in Unsplash and then create an application. 

If those values are not set, the Unsplash view won't be displayed and a console log will inform.

This configuration, as any other OSGI configuration, can be modified from Liferay Control Panel - Configuration - Configuration Admin. There, you will need to search `Unsplash item selector configuration` and you can set the values there.

This Unsplash View will be displayed in Liferay whenever Item Selector porltet is invoked with `ImageItemSelectorCriterion` criterion and the desired return types contains `URLItemSelectorReturnType`. This includes Blogs and Web Content in an OOTB installation of Liferay 7.
