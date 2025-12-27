(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['kotlin-kotlin-stdlib'] = factory(typeof this['kotlin-kotlin-stdlib'] === 'undefined' ? {} : this['kotlin-kotlin-stdlib']);
}(this, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  function protoOf(constructor) {
    return constructor.prototype;
  }
  function defineProp(obj, name, getter, setter) {
    return Object.defineProperty(obj, name, {configurable: true, get: getter, set: setter});
  }
  var VOID;
  return _;
}));

//# sourceMappingURL=kotlin-kotlin-stdlib.js.map
