(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['compose-multiplatform-core-animation-core'] = factory(typeof this['compose-multiplatform-core-animation-core'] === 'undefined' ? {} : this['compose-multiplatform-core-animation-core']);
}(this, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  var UNSET;
  //region block: init
  UNSET = 3.4028235E38;
  //endregion
  return _;
}));

//# sourceMappingURL=compose-multiplatform-core-animation-core.js.map
