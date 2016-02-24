(function(global, $) {
  $.when($.getJSON('assets/js/lang/' + $conn.getLocale() + '.json'))
  .then(function(data){
      global.lang=new Polyglot({locale: $conn.getLocale(), phrases: data});
      render();
  })
})(window, jQuery);
