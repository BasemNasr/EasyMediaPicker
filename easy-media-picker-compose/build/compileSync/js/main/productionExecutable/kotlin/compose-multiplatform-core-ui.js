(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['compose-multiplatform-core-ui'] = factory(typeof this['compose-multiplatform-core-ui'] === 'undefined' ? {} : this['compose-multiplatform-core-ui']);
}(this, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  var DefaultCacheSize;
  var defaultCanvasElementId;
  //region block: init
  DefaultCacheSize = 8;
  defaultCanvasElementId = 'ComposeTarget';
  //endregion
  return _;
}));

//# sourceMappingURL=compose-multiplatform-core-ui.js.map
