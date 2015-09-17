;(function() {
	var PATH_UNSPLASH_ITEM_SELECTOR = Liferay.ThemeDisplay.getPathContext() + '/o/unsplash-item-selector-web';

	AUI().applyConfig(
		{
			groups: {
				unsplash: {
					base: PATH_UNSPLASH_ITEM_SELECTOR + '/js/',
					modules: {
						'unsplash-item-selector': {
							path: 'unsplash_item_selector.js',
							requires: [
								'liferay-item-viewer',
								'liferay-portlet-base'
							]
						}
					},
					root: PATH_UNSPLASH_ITEM_SELECTOR + '/js/'
				}
			}
		}
	);
})();