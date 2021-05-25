// Extend jQuery
(function($)
{
  // Enable or disable an element
  $.fn.setReadOnly = function(bReadOnly)
  {
    return $(this).each(function()
    {
      if (bReadOnly)
        $(this).attr("readonly", "readonly");
      else
        $(this).removeAttr("readonly");
    });
  };
})(jQuery);
