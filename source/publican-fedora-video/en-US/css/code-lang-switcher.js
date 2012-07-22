$($(function(){
var language_css_codes = {
    'JavaScript':'js',
    'Node.js' :'node',
    'C++': 'cpp',
    'C#/.NET' : 'cs',
    'Python': 'py',
    'Ruby': 'rb',
    'Java': 'java',
    'html': 'HTML'
  };
  $('div.code-div').each(function() {
    var div_code = $(this);
    var counter = 0;
    var ul = $(document.createElement('ul'));
    div_code.find('dt').each(function() {
      lang = $(this).children('.term')[0].innerHTML
      var linkEl = $('<a href="#" rel="'+language_css_codes[lang]+'">'+lang+'</a>');
      var listItemEl = $('<li class="code-select-li"></li>');
      // Right now it's adding them in the order the author put them
      // Probably should sort alphabetically
      listItemEl.append(linkEl);
      ul.append(listItemEl);
      proglisting = $(this).next();
      proglisting.addClass('code-lang-' + language_css_codes[lang]);
      
      $(this).detach();

      linkEl.click(function(e) {
        e.preventDefault();
        
        if ($(this).hasClass('active')) return;
        
        div_code.find('dd').hide();
        div_code.find('.code-lang-'+$(this).attr('rel')).show();
        div_code.find('a').removeClass('active');
        $(this).addClass('active');
      });
      
      // This should store the lang pref (perhaps in a cookie) and show that by default on page load
      div_code.prepend(ul)
      div_code.find('dd:not(:first)').hide();
      div_code.find('a:first').addClass('active');
    });
  });
}))
