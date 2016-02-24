var $conn = (function () {

    return {

        get: function (url, callback) {
            $.ajax({
                url: url,
                headers: {
                    'Content-Type': "application/json"
                },
                type: 'GET',
                dataType: 'json',
                success: function (data) {
                    callback(data)
                },
                error: function (xhr, status, err) {
                    console.error(xhr.status, err.toString());
                }

            });
        },

        post: function (url, params, callback) {
            $.ajax({
                url: url,
                headers: {
                    'Content-Type': "application/json"
                },
                type: 'POST',
                data: params,
                success: function (data) {
                    callback(data)
                },
                error: function (xhr, status, err) {
                    console.log(xhr);
                }
            });
        },

        put: function (url, params, callback) {
            $.ajax({
                url: url,
                headers: {
                    'Content-Type': "application/json"
                },
                type: 'PUT',
                data: params,
                success: function (data) {
                    callback(data)
                },
                error: function (xhr, status, err) {
                    console.error(xhr.status, err.toString());
                }
            });
        },

        getLocale:function(){
            var locale=window.localStorage.getItem('locale') || "en";
            if(locale=="undefined")
                locale="en";
            return locale;
        },

        setLocale:function(value){
            window.localStorage.setItem('locale',value);
        }
    }
})();