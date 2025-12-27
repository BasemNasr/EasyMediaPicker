(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['compose-multiplatform-core-runtime'] = factory(typeof this['compose-multiplatform-core-runtime'] === 'undefined' ? {} : this['compose-multiplatform-core-runtime']);
}(this, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  return _;
}));

//# sourceMappingURL=compose-multiplatform-core-runtime.js.map
