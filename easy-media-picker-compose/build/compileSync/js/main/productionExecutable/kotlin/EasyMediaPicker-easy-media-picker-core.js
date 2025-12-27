(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['EasyMediaPicker-easy-media-picker-core'] = factory(typeof this['EasyMediaPicker-easy-media-picker-core'] === 'undefined' ? {} : this['EasyMediaPicker-easy-media-picker-core']);
}(this, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  return _;
}));

//# sourceMappingURL=EasyMediaPicker-easy-media-picker-core.js.map
