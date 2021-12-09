(function(){function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s}return e})()({"/Users/arisjiratkurniawan/projects/indikasi-geografis/resources/frontend/src/scripts/components/dropdownMenu.component.js":[function(require,module,exports){
'use strict';

var dropdownMenu = {
  state: '',
  init: function init() {
    this.elClose();
    this.dom();
    this.eventList();
  },
  dom: function dom() {
    this.btnDropdown = $('[js-direction="dropdown-menu"][js-target]');
    this.btnDropdownOut = $('.dropdown-out');
  },
  eventList: function eventList() {
    this.btnDropdown.on('click', this.dropdownMenu.bind(this));
    this.btnDropdownOut.on('click', this.dropdownMenuOut.bind(this));
  },
  elClose: function elClose() {
    $('body').append('<div class="dropdown-out"></div>');
  },
  dropdownMenu: function dropdownMenu(el) {
    this.btnDropdownOut.show();
    var $target = el.target.getAttribute('js-target');
    var $action = el.target.getAttribute('js-action');

    if ($action === 'open') {
      $('[js-direction="dropdown-menu"][js-target="' + $target + '"]').attr('js-action', 'close').addClass('active');
      $('[js-direction="dropdown-menu-place"][js-place="' + $target + '"]').slideDown();
    }
    if ($action === 'close') {
      $('[js-direction="dropdown-menu"][js-target="' + $target + '"]').attr('js-action', 'open').removeClass('active');
      $('[js-direction="dropdown-menu-place"][js-place="' + $target + '"]').slideUp();
      $('[js-direction="dropdown-menu-place"][js-place="' + $target + '"] [js-direction="dropdown-menu-place"]').slideUp();
      $('[js-direction="dropdown-menu-place"][js-place="' + $target + '"] [js-direction="dropdown-menu"]').attr('js-action', 'open').removeClass('active');
    }
  },
  dropdownMenuOut: function dropdownMenuOut() {
    this.btnDropdownOut.hide();
    $('[js-direction="dropdown-menu"][js-target]').attr('js-action', 'open').removeClass('active');
    $('[js-direction="dropdown-menu-place"][js-place]').slideUp();
    $('[js-direction="dropdown-menu-place"][js-place] [js-direction="dropdown-menu-place"]').slideUp();
    $('[js-direction="dropdown-menu-place"][js-place] [js-direction="dropdown-menu"]').attr('js-action', 'open').removeClass('active');
  }
};

dropdownMenu.init();




},{}],"/Users/arisjiratkurniawan/projects/indikasi-geografis/resources/frontend/src/scripts/components/sidebar.component.js":[function(require,module,exports){
'use strict';

var sidebar = {
  state: {
    stats: false
  },
  init: function init() {
    this.dom();
    this.eventList();
    if (this.placeSidebar.length < 1) this.btnSidebar.parent().remove();
  },
  dom: function dom() {
    this.btnSidebar = $('[js-direction="sidebar-wrap"]');
    this.placeSidebar = $('[js-direction="sidebar-wrap-place"]');
  },
  eventList: function eventList() {
    this.btnSidebar.on('click', this.sidebar.bind(this));
  },
  sidebar: function sidebar(el) {
    if (this.state.stats) {
      this.state.stats = false;

      this.placeSidebar.removeClass('active');
    } else {
      this.state.stats = true;

      this.placeSidebar.addClass('active');
    }
  }
};

sidebar.init();


},{}],"/Users/arisjiratkurniawan/projects/indikasi-geografis/resources/frontend/src/scripts/components/stepForm.component.js":[function(require,module,exports){
'use strict';

var stepForm = {
  state: {
    currentStep: '1'
  },
  init: function init() {
    this.dom();
    this.stepFormElementDefault();
    this.stepFormLoad();
    this.eventList();
    // localStorage.removeItem('stepFormCurrent')
  },
  dom: function dom() {
    this.buttonAction = $('[js-direction="step-form-button"]');
    this.buttonBack = $('[js-direction="step-form-button-back"]');
    this.stepNav = $('li[js-direction="step-form"]');
    this.stepPlace = $('[js-direction="step-form-place"]');
  },
  eventList: function eventList() {
    this.buttonAction.on('click', this.stepFormButtonAction.bind(this));
    this.buttonBack.on('click', this.stepFormButtonBack.bind(this));
    this.stepNav.on('click', this.stepFormButtonControll.bind(this));
  },
  stepFormLoad: function stepFormLoad() {
    // if (!localStorage.getItem('stepFormCurrent')) {
    //   this.state.currentStep = 1
    // } else {
    //   this.state.currentStep = localStorage.getItem('stepFormCurrent')
    // }
    this.stepForm(this.state.currentStep);
  },
  stepForm: function stepForm(current) {
    // localStorage.setItem('stepFormCurrent', this.state.currentStep)
    this.stepFormElement(this.state.currentStep);
    this.stepFormElementButton(this.state.currentStep);
  },
  stepFormElementDefault: function stepFormElementDefault() {
    // this.stepNav.removeClass('current yet');
    this.stepPlace.addClass('display--none');
  },

  stepFormElement: function stepFormElement(current) {
    $('[js-direction="step-form"][js-id="' + current + '"]').addClass('current');
    $('[js-direction="step-form-place"][js-id="' + current + '"]').removeClass('display--none');
//    var data = confirm("a");
//    if(data){
//        $('[js-direction="step-form"][js-id="' + current + '"]').addClass('current');
//        $('[js-direction="step-form-place"][js-id="' + current + '"]').removeClass('display--none');
//    }else{
//        $('[js-direction="step-form"][js-id="' + current + '"]').addClass('default');
//        $('[js-direction="step-form-place"][js-id="' + current + '"]').removeClass('current');
//    }
    // if (current > 1) {
      // for (var i = 1; i <= current; i++) {
        // $('[js-direction="step-form"][js-id="' + i + '"]').addClass('yet');
      // }
    // }
  },

  stepFormElementButton: function stepFormElementButton(current) {
    if (current < 2) {
      this.buttonBack.addClass('display--none');
      this.buttonAction.html('Lanjutkan');
    }
    if (current == 8) {
      this.buttonAction.html('Submit');
    }
    if (current >= 2) {
      this.buttonBack.removeClass('display--none');
      this.buttonAction.html('Lanjutkan');
    }
  },
  stepFormButtonAction: function stepFormButtonAction(elem) {
    var value = this.state.currentStep;
    var action = value * 1 + 1;

    if (action <= 8) {
      $.ajax({
        success: function success(response) {
          stepForm.state.currentStep = action;
          // localStorage.setItem('stepFormCurrent', stepForm.state.currentStep)

          stepForm.stepFormElementDefault();
          stepForm.stepFormElement(stepForm.state.currentStep);
          stepForm.stepFormElementButton(stepForm.state.currentStep);
        }
      });
    } else {
      this.stepFormElementButton(this.state.currentStep);
      this.stepFormSubmit();
    }
  },
  stepFormButtonBack: function stepFormButtonBack(elem) {
    var value = this.state.currentStep;
    var action = value * 1 - 1;

    if (action >= 1) {
      this.state.currentStep = action;
      // localStorage.setItem('stepFormCurrent', this.state.currentStep)

      this.stepFormElementDefault();
      this.stepFormElement(this.state.currentStep);
      this.stepFormElementButton(this.state.currentStep);
    }
  },
  stepFormSubmit: function stepFormSubmit() {
    $.ajax({
      success: function success(response) {
        alert('Do something...');
        stepForm.state.currentStep = 0;
        // localStorage.removeItem('stepFormCurrent')
        window.location.reload();
      }
    });
  },
  stepFormButtonControll: function stepFormButtonControll(el) {
    var target = el.currentTarget.getAttribute('js-id');
    var current = this.state.currentStep;
    var row = this.stepNav.length;
    var notice = "Tombol ini hanya untuk navigasi, jika anda menekan tombol ini, maka data anda tidak akan tersimpan. Untuk menyimpan data klik tombol Simpan dan Lanjutkan. Pastikan data anda tersimpan.";

//    if(confirm(notice)==true){
        this.state.currentStep = target;
        this.stepFormElementDefault();
        this.stepForm(target);
        this.stepFormElementButton(target);
        this.stepFormButtonControllElement(target);
//    }else{
//        this.state.currentStep = current;
//        this.stepFormElementDefault();
//        this.stepForm(current);
//        this.stepFormElementButton(current);
//        this.stepFormButtonControllElement(current);
//    }
  },
  stepFormButtonControllElement: function stepFormButtonControllElement(index) {
    $('[js-direction="step-form"][js-id]').removeClass('current');
    // for (var i = 1; i < index; i++) {
      // $('[js-direction="step-form"][js-id="' + i + '"]').addClass('yet');
    // }
    $('[js-direction="step-form"][js-id="' + index + '"]').addClass('current');
    $('[js-direction="step-form-place"][js-id!="' + index + '"]').addClass('display--none');
    $('[js-direction="step-form-place"][js-id="' + index + '"]').removeClass('display--none');
  }
};

stepForm.init();
window.stepFrom = stepForm;



},{}],"/Users/arisjiratkurniawan/projects/indikasi-geografis/resources/frontend/src/scripts/main.js":[function(require,module,exports){
'use strict';



require('./views/accordion');

require('./components/stepForm.component');

require('./components/dropdownMenu.component');

require('./components/sidebar.component');



var testing = true;
$('.navbar-text').addClass('display--none');
$('#testLogin').click(function () {
  if (testing) {
    $('.navbar--menu').removeClass('display--none');
    $('.navbar-text').addClass('display--none');
    testing = false;
  } else {
    $('.navbar-text').removeClass('display--none');
    $('.navbar--menu').addClass('display--none');
    testing = true;
  }
});
/* ########################### */
$('.ui-accordion').accordion();
$(".detail-document .nav-tabs a").click(function () {
  $(this).tab('show');
});

},{"./components/dropdownMenu.component":"/Users/arisjiratkurniawan/projects/indikasi-geografis/resources/frontend/src/scripts/components/dropdownMenu.component.js","./components/fileGet.component":"/Users/arisjiratkurniawan/projects/indikasi-geografis/resources/frontend/src/scripts/components/fileGet.component.js","./components/formMap.component":"/Users/arisjiratkurniawan/projects/indikasi-geografis/resources/frontend/src/scripts/components/formMap.component.js","./components/sidebar.component":"/Users/arisjiratkurniawan/projects/indikasi-geografis/resources/frontend/src/scripts/components/sidebar.component.js","./components/skin.component":"/Users/arisjiratkurniawan/projects/indikasi-geografis/resources/frontend/src/scripts/components/skin.component.js","./components/stepForm.component":"/Users/arisjiratkurniawan/projects/indikasi-geografis/resources/frontend/src/scripts/components/stepForm.component.js","./views/accordion":"/Users/arisjiratkurniawan/projects/indikasi-geografis/resources/frontend/src/scripts/views/accordion.js","./views/app.map":"/Users/arisjiratkurniawan/projects/indikasi-geografis/resources/frontend/src/scripts/views/app.map.js","./views/cluster.map":"/Users/arisjiratkurniawan/projects/indikasi-geografis/resources/frontend/src/scripts/views/cluster.map.js","./views/google.map.mutant":"/Users/arisjiratkurniawan/projects/indikasi-geografis/resources/frontend/src/scripts/views/google.map.mutant.js","./views/leaflet":"/Users/arisjiratkurniawan/projects/indikasi-geografis/resources/frontend/src/scripts/views/leaflet.js"}],"/Users/arisjiratkurniawan/projects/indikasi-geografis/resources/frontend/src/scripts/views/accordion.js":[function(require,module,exports){
'use strict';

(function ($, window, document, undefined) {

  'use strict';

  window = typeof window != 'undefined' && window.Math == Math ? window : typeof self != 'undefined' && self.Math == Math ? self : Function('return this')();

  $.fn.accordion = function (parameters) {
    var $allModules = $(this),
        time = new Date().getTime(),
        performance = [],
        query = arguments[0],
        methodInvoked = typeof query == 'string',
        queryArguments = [].slice.call(arguments, 1),
        requestAnimationFrame = window.requestAnimationFrame || window.mozRequestAnimationFrame || window.webkitRequestAnimationFrame || window.msRequestAnimationFrame || function (callback) {
      setTimeout(callback, 0);
    },
        returnedValue;
    $allModules.each(function () {
      var settings = $.isPlainObject(parameters) ? $.extend(true, {}, $.fn.accordion.settings, parameters) : $.extend({}, $.fn.accordion.settings),
          className = settings.className,
          namespace = settings.namespace,
          selector = settings.selector,
          error = settings.error,
          eventNamespace = '.' + namespace,
          moduleNamespace = 'module-' + namespace,
          moduleSelector = $allModules.selector || '',
          $module = $(this),
          $title = $module.find(selector.title),
          $content = $module.find(selector.content),
          element = this,
          instance = $module.data(moduleNamespace),
          observer,
          module;

      module = {

        initialize: function initialize() {
          module.debug('Initializing', $module);
          module.bind.events();
          if (settings.observeChanges) {
            module.observeChanges();
          }
          module.instantiate();
        },

        instantiate: function instantiate() {
          instance = module;
          $module.data(moduleNamespace, module);
        },

        destroy: function destroy() {
          module.debug('Destroying previous instance', $module);
          $module.off(eventNamespace).removeData(moduleNamespace);
        },

        refresh: function refresh() {
          $title = $module.find(selector.title);
          $content = $module.find(selector.content);
        },

        observeChanges: function observeChanges() {
          if ('MutationObserver' in window) {
            observer = new MutationObserver(function (mutations) {
              module.debug('DOM tree modified, updating selector cache');
              module.refresh();
            });
            observer.observe(element, {
              childList: true,
              subtree: true
            });
            module.debug('Setting up mutation observer', observer);
          }
        },

        bind: {
          events: function events() {
            module.debug('Binding delegated events');
            $module.on(settings.on + eventNamespace, selector.trigger, module.event.click);
          }
        },

        event: {
          click: function click() {
            module.toggle.call(this);
          }
        },

        toggle: function toggle(query) {
          var $activeTitle = query !== undefined ? typeof query === 'number' ? $title.eq(query) : $(query).closest(selector.title) : $(this).closest(selector.title),
              $activeContent = $activeTitle.next($content),
              isAnimating = $activeContent.hasClass(className.animating),
              isActive = $activeContent.hasClass(className.active),
              isOpen = isActive && !isAnimating,
              isOpening = !isActive && isAnimating;
          module.debug('Toggling visibility of content', $activeTitle);
          if (isOpen || isOpening) {
            if (settings.collapsible) {
              module.close.call($activeTitle);
            } else {
              module.debug('Cannot close accordion content collapsing is disabled');
            }
          } else {
            module.open.call($activeTitle);
          }
        },

        open: function open(query) {
          var $activeTitle = query !== undefined ? typeof query === 'number' ? $title.eq(query) : $(query).closest(selector.title) : $(this).closest(selector.title),
              $activeContent = $activeTitle.next($content),
              isAnimating = $activeContent.hasClass(className.animating),
              isActive = $activeContent.hasClass(className.active),
              isOpen = isActive || isAnimating;
          if (isOpen) {
            module.debug('Accordion already open, skipping', $activeContent);
            return;
          }
          module.debug('Opening accordion content', $activeTitle);
          settings.onOpening.call($activeContent);
          settings.onChanging.call($activeContent);
          if (settings.exclusive) {
            module.closeOthers.call($activeTitle);
          }
          $activeTitle.addClass(className.active);
          $activeContent.stop(true, true).addClass(className.animating);
          if (settings.animateChildren) {
            if ($.fn.transition !== undefined && $module.transition('is supported')) {
              $activeContent.children().transition({
                animation: 'fade in',
                queue: false,
                useFailSafe: true,
                debug: settings.debug,
                verbose: settings.verbose,
                duration: settings.duration
              });
            } else {
              $activeContent.children().stop(true, true).animate({
                opacity: 1
              }, settings.duration, module.resetOpacity);
            }
          }
          $activeContent.slideDown(settings.duration, settings.easing, function () {
            $activeContent.removeClass(className.animating).addClass(className.active);
            module.reset.display.call(this);
            settings.onOpen.call(this);
            settings.onChange.call(this);
          });
        },

        close: function close(query) {
          var $activeTitle = query !== undefined ? typeof query === 'number' ? $title.eq(query) : $(query).closest(selector.title) : $(this).closest(selector.title),
              $activeContent = $activeTitle.next($content),
              isAnimating = $activeContent.hasClass(className.animating),
              isActive = $activeContent.hasClass(className.active),
              isOpening = !isActive && isAnimating,
              isClosing = isActive && isAnimating;
          if ((isActive || isOpening) && !isClosing) {
            module.debug('Closing accordion content', $activeContent);
            settings.onClosing.call($activeContent);
            settings.onChanging.call($activeContent);
            $activeTitle.removeClass(className.active);
            $activeContent.stop(true, true).addClass(className.animating);
            if (settings.animateChildren) {
              if ($.fn.transition !== undefined && $module.transition('is supported')) {
                $activeContent.children().transition({
                  animation: 'fade out',
                  queue: false,
                  useFailSafe: true,
                  debug: settings.debug,
                  verbose: settings.verbose,
                  duration: settings.duration
                });
              } else {
                $activeContent.children().stop(true, true).animate({
                  opacity: 0
                }, settings.duration, module.resetOpacity);
              }
            }
            $activeContent.slideUp(settings.duration, settings.easing, function () {
              $activeContent.removeClass(className.animating).removeClass(className.active);
              module.reset.display.call(this);
              settings.onClose.call(this);
              settings.onChange.call(this);
            });
          }
        },

        closeOthers: function closeOthers(index) {
          var $activeTitle = index !== undefined ? $title.eq(index) : $(this).closest(selector.title),
              $parentTitles = $activeTitle.parents(selector.content).prev(selector.title),
              $activeAccordion = $activeTitle.closest(selector.accordion),
              activeSelector = selector.title + '.' + className.active + ':visible',
              activeContent = selector.content + '.' + className.active + ':visible',
              $openTitles,
              $nestedTitles,
              $openContents;
          if (settings.closeNested) {
            $openTitles = $activeAccordion.find(activeSelector).not($parentTitles);
            $openContents = $openTitles.next($content);
          } else {
            $openTitles = $activeAccordion.find(activeSelector).not($parentTitles);
            $nestedTitles = $activeAccordion.find(activeContent).find(activeSelector).not($parentTitles);
            $openTitles = $openTitles.not($nestedTitles);
            $openContents = $openTitles.next($content);
          }
          if ($openTitles.length > 0) {
            module.debug('Exclusive enabled, closing other content', $openTitles);
            $openTitles.removeClass(className.active);
            $openContents.removeClass(className.animating).stop(true, true);
            if (settings.animateChildren) {
              if ($.fn.transition !== undefined && $module.transition('is supported')) {
                $openContents.children().transition({
                  animation: 'fade out',
                  useFailSafe: true,
                  debug: settings.debug,
                  verbose: settings.verbose,
                  duration: settings.duration
                });
              } else {
                $openContents.children().stop(true, true).animate({
                  opacity: 0
                }, settings.duration, module.resetOpacity);
              }
            }
            $openContents.slideUp(settings.duration, settings.easing, function () {
              $(this).removeClass(className.active);
              module.reset.display.call(this);
            });
          }
        },

        reset: {

          display: function display() {
            module.verbose('Removing inline display from element', this);
            $(this).css('display', '');
            if ($(this).attr('style') === '') {
              $(this).attr('style', '').removeAttr('style');
            }
          },

          opacity: function opacity() {
            module.verbose('Removing inline opacity from element', this);
            $(this).css('opacity', '');
            if ($(this).attr('style') === '') {
              $(this).attr('style', '').removeAttr('style');
            }
          }

        },

        setting: function setting(name, value) {
          module.debug('Changing setting', name, value);
          if ($.isPlainObject(name)) {
            $.extend(true, settings, name);
          } else if (value !== undefined) {
            if ($.isPlainObject(settings[name])) {
              $.extend(true, settings[name], value);
            } else {
              settings[name] = value;
            }
          } else {
            return settings[name];
          }
        },
        internal: function internal(name, value) {
          module.debug('Changing internal', name, value);
          if (value !== undefined) {
            if ($.isPlainObject(name)) {
              $.extend(true, module, name);
            } else {
              module[name] = value;
            }
          } else {
            return module[name];
          }
        },
        debug: function debug() {
          if (!settings.silent && settings.debug) {
            if (settings.performance) {
              module.performance.log(arguments);
            } else {
              module.debug = Function.prototype.bind.call(console.info, console, settings.name + ':');
              module.debug.apply(console, arguments);
            }
          }
        },
        verbose: function verbose() {
          if (!settings.silent && settings.verbose && settings.debug) {
            if (settings.performance) {
              module.performance.log(arguments);
            } else {
              module.verbose = Function.prototype.bind.call(console.info, console, settings.name + ':');
              module.verbose.apply(console, arguments);
            }
          }
        },
        error: function error() {
          if (!settings.silent) {
            module.error = Function.prototype.bind.call(console.error, console, settings.name + ':');
            module.error.apply(console, arguments);
          }
        },
        performance: {
          log: function log(message) {
            var currentTime, executionTime, previousTime;
            if (settings.performance) {
              currentTime = new Date().getTime();
              previousTime = time || currentTime;
              executionTime = currentTime - previousTime;
              time = currentTime;
              performance.push({
                'Name': message[0],
                'Arguments': [].slice.call(message, 1) || '',
                'Element': element,
                'Execution Time': executionTime
              });
            }
            clearTimeout(module.performance.timer);
            module.performance.timer = setTimeout(module.performance.display, 500);
          },
          display: function display() {
            var title = settings.name + ':',
                totalTime = 0;
            time = false;
            clearTimeout(module.performance.timer);
            $.each(performance, function (index, data) {
              totalTime += data['Execution Time'];
            });
            title += ' ' + totalTime + 'ms';
            if (moduleSelector) {
              title += ' \'' + moduleSelector + '\'';
            }
            if ((console.group !== undefined || console.table !== undefined) && performance.length > 0) {
              console.groupCollapsed(title);
              if (console.table) {
                console.table(performance);
              } else {
                $.each(performance, function (index, data) {
                  console.log(data['Name'] + ': ' + data['Execution Time'] + 'ms');
                });
              }
              console.groupEnd();
            }
            performance = [];
          }
        },
        invoke: function invoke(query, passedArguments, context) {
          var object = instance,
              maxDepth,
              found,
              response;
          passedArguments = passedArguments || queryArguments;
          context = element || context;
          if (typeof query == 'string' && object !== undefined) {
            query = query.split(/[\. ]/);
            maxDepth = query.length - 1;
            $.each(query, function (depth, value) {
              var camelCaseValue = depth != maxDepth ? value + query[depth + 1].charAt(0).toUpperCase() + query[depth + 1].slice(1) : query;
              if ($.isPlainObject(object[camelCaseValue]) && depth != maxDepth) {
                object = object[camelCaseValue];
              } else if (object[camelCaseValue] !== undefined) {
                found = object[camelCaseValue];
                return false;
              } else if ($.isPlainObject(object[value]) && depth != maxDepth) {
                object = object[value];
              } else if (object[value] !== undefined) {
                found = object[value];
                return false;
              } else {
                module.error(error.method, query);
                return false;
              }
            });
          }
          if ($.isFunction(found)) {
            response = found.apply(context, passedArguments);
          } else if (found !== undefined) {
            response = found;
          }
          if ($.isArray(returnedValue)) {
            returnedValue.push(response);
          } else if (returnedValue !== undefined) {
            returnedValue = [returnedValue, response];
          } else if (response !== undefined) {
            returnedValue = response;
          }
          return found;
        }
      };
      if (methodInvoked) {
        if (instance === undefined) {
          module.initialize();
        }
        module.invoke(query);
      } else {
        if (instance !== undefined) {
          instance.invoke('destroy');
        }
        module.initialize();
      }
    });
    return returnedValue !== undefined ? returnedValue : this;
  };

  $.fn.accordion.settings = {

    name: 'Accordion',
    namespace: 'accordion',

    silent: false,
    debug: false,
    verbose: false,
    performance: true,

    on: 'click', // event on title that opens accordion

    observeChanges: true, // whether accordion should automatically refresh on DOM insertion

    exclusive: true, // whether a single accordion content panel should be open at once
    collapsible: true, // whether accordion content can be closed
    closeNested: false, // whether nested content should be closed when a panel is closed
    animateChildren: true, // whether children opacity should be animated

    duration: 350, // duration of animation
    easing: 'easeOutQuad', // easing equation for animation

    onOpening: function onOpening() {}, // callback before open animation
    onClosing: function onClosing() {}, // callback before closing animation
    onChanging: function onChanging() {}, // callback before closing or opening animation

    onOpen: function onOpen() {}, // callback after open animation
    onClose: function onClose() {}, // callback after closing animation
    onChange: function onChange() {}, // callback after closing or opening animation

    error: {
      method: 'The method you called is not defined'
    },

    className: {
      active: 'active',
      animating: 'animating'
    },

    selector: {
      accordion: '.ui-accordion',
      title: '.ui-accordion--title',
      trigger: '.ui-accordion--title',
      content: '.ui-accordion--content'
    }

  };

  // Adds easing
  $.extend($.easing, {
    easeOutQuad: function easeOutQuad(x, t, b, c, d) {
      return -c * (t /= d) * (t - 2) + b;
    }
  });
})(jQuery, window, document);


},{}],"/Users/arisjiratkurniawan/projects/indikasi-geografis/resources/frontend/src/scripts/views/items.db.js":[function(require,module,exports){
'use strict';

exports.default = data;


var _typeof2 = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _typeof = typeof Symbol === "function" && _typeof2(Symbol.iterator) === "symbol" ? function (obj) {
  return typeof obj === "undefined" ? "undefined" : _typeof2(obj);
} : function (obj) {
  return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj === "undefined" ? "undefined" : _typeof2(obj);
};

},{}]},{},["/Users/arisjiratkurniawan/projects/indikasi-geografis/resources/frontend/src/scripts/main.js"])