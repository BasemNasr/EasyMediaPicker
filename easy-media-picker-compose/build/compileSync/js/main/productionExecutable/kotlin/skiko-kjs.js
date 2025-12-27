(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports', './kotlin-kotlin-stdlib.js'], factory);
  else if (typeof exports === 'object')
    factory(module.exports, require('./kotlin-kotlin-stdlib.js'));
  else {
    if (typeof this['kotlin-kotlin-stdlib'] === 'undefined') {
      throw new Error("Error loading module 'skiko-kjs'. Its dependency 'kotlin-kotlin-stdlib' was not found. Please, check whether 'kotlin-kotlin-stdlib' is loaded prior to 'skiko-kjs'.");
    }
    root['skiko-kjs'] = factory(typeof this['skiko-kjs'] === 'undefined' ? {} : this['skiko-kjs'], this['kotlin-kotlin-stdlib']);
  }
}(this, function (_, kotlin_kotlin) {
  'use strict';
  //region block: imports
  var ArrayList_init_$Create$ = kotlin_kotlin.$_$.b;
  var protoOf = kotlin_kotlin.$_$.d;
  var classMeta = kotlin_kotlin.$_$.c;
  var setMetadataFor = kotlin_kotlin.$_$.e;
  var VOID = kotlin_kotlin.$_$.a;
  //endregion
  //region block: pre-declaration
  setMetadataFor(InteropScope, 'InteropScope', classMeta, VOID, VOID, InteropScope);
  //endregion
  var INTEROP_SCOPE;
  var interopScopeCounter;
  function InteropScope() {
    var tmp = this;
    // Inline function 'kotlin.collections.mutableListOf' call
    tmp.j1_1 = ArrayList_init_$Create$();
    this.k1_1 = false;
  }
  function _createLocalCallbackScope$accessor$wmqves() {
    _init_properties_Native_js_kt__80argu();
    return _createLocalCallbackScope();
  }
  function _releaseLocalCallbackScope$accessor$wmqves() {
    _init_properties_Native_js_kt__80argu();
    return _releaseLocalCallbackScope();
  }
  var properties_initialized_Native_js_kt_fdhhkg;
  function _init_properties_Native_js_kt__80argu() {
    if (!properties_initialized_Native_js_kt_fdhhkg) {
      properties_initialized_Native_js_kt_fdhhkg = true;
      INTEROP_SCOPE = new InteropScope();
      interopScopeCounter = 0;
    }
  }
  var onContentScaleChanged;
  //region block: init
  onContentScaleChanged = null;
  //endregion
  return _;
}));

//# sourceMappingURL=skiko-kjs.js.map
