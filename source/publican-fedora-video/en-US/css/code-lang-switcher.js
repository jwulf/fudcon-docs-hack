

$($(function(){

function addNewListItem($list, $ele) {
    $list.append($ele);
    var listItems = $list.children('li').get();
    listItems.sort(function(a, b) {
       return $(a).text().toUpperCase().localeCompare($(b).text().toUpperCase());
    });
    $.each(listItems, function(idx, itm) { $list.append(itm); });
}

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
  $('div.codetabs').each(function() {
    var div_code = $(this);
    var counter = 0;
    var ul = $(document.createElement('ul'));
    div_code.find('dt').each(function() {
      lang = $(this).children('.term')[0].innerHTML
      var linkEl = $('<a href="#" rel="'+language_css_codes[lang]+'">'+lang+'</a>');
      var listItemEl = $('<li class="code-select-li"></li>');
      // Alpha sort idea from here: http://stackoverflow.com/questions/11509555/insert-new-list-sorted-in-alphabetical-order-javascript-or-jquery
      listItemEl.append(linkEl);
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
  
            addNewListItem(ul, listItemEl);
            // This should store the lang pref (perhaps in a cookie) and show that by default on page load
    });
      div_code.prepend(ul)
      initialtab = div_code.find('a:first');
      initialtab.addClass('active');
      div_code.find('dd').hide();
      div_code.find('.code-lang-'+ initialtab.attr('rel')).show();
  });
}))
