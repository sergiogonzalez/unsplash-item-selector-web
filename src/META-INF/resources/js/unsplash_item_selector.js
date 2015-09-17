AUI.add(
	'unsplash-item-selector',
	function(A) {
		var Lang = A.Lang;

		var ITEM_LINK_TPL = '<a data-returnType="URL" data-value="{value}" href="{preview}"></a>';

		var STR_LINKS = 'links';

		var STR_SELECTED_ITEM = 'selectedItem';

		var STR_VISIBLE_CHANGE = 'visibleChange';

		var UnsplashItemSelector = A.Component.create(
			{
				ATTRS: {
					closeCaption: {
						validator: Lang.isString,
						value: ''
					}
				},

				AUGMENTS: [Liferay.PortletBase],

				EXTENDS: A.Base,

				NAME: 'unsplashritemselector',

				prototype: {
					initializer: function() {
						var instance = this;

						instance._itemViewer = new A.LiferayItemViewer(
							{
								btnCloseCaption: instance.get('closeCaption'),
								caption: '',
								links: '',
								renderControls: false
							}
						);

						instance._unsplashImageSelectorWrapper = instance.one('#unsplashImageSelectorWrapper');

						instance._bindUI();
						instance._renderUI();
					},

					destructor: function() {
						var instance = this;

						instance._itemViewer.destroy();

						(new A.EventHandle(instance._eventHandles)).detach();
					},

					_afterVisibleChange: function(event) {
						var instance = this;

						if (!event.newVal) {
							instance.fire(STR_SELECTED_ITEM);
						}
					},

					_bindUI: function() {
						var instance = this;

						var itemViewer = instance._itemViewer;

						instance._eventHandles = [
							itemViewer.after(STR_VISIBLE_CHANGE, instance._afterVisibleChange, instance),
							itemViewer.on('animate', instance._onItemSelected, instance),
							instance._unsplashImageSelectorWrapper.delegate('click', instance._previewItem, '.unsplash-image', instance)
						];
					},

					_onItemSelected: function() {
						var instance = this;

						var itemViewer = instance._itemViewer;

						var link = itemViewer.get(STR_LINKS).item(itemViewer.get('currentIndex'));

						instance.fire(
							STR_SELECTED_ITEM,
							{
								data: {
									returnType: link.getData('returnType'),
									value: link.getData('value')
								}
							}
						);
					},

					_previewItem: function(event) {
						var instance = this;

						var url = event.currentTarget.attr('data-url');
						var title = event.currentTarget.attr('data-title');

						if (url) {
							var linkNode = A.Node.create(
								Lang.sub(
									ITEM_LINK_TPL,
									{
										preview: url,
										title: title,
										value: url
									}
								)
							);

							var itemViewer = instance._itemViewer;

							itemViewer.set(STR_LINKS, new A.NodeList(linkNode));

							itemViewer.show();
						}
					},

					_renderUI: function() {
						var instance = this;

						var rootNode = instance.rootNode;

						instance._itemViewer.render(rootNode);
					}
				}
			}
		);

		Liferay.UnsplashItemSelector = UnsplashItemSelector;
	},
	'',
	{
		requires: ['liferay-item-viewer', 'liferay-portlet-base']
	}
);