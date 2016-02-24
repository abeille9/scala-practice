var MainLayout = React.createClass({
    displayName: 'MainLayout',

    componentDidMount: function () {
        $('#side-menu').metisMenu();
    },
    renderListItems: function (menu) {
        var items = [];
        self = this;
        if (menu) {
            $.each(menu, function (i, item) {
                if (item.items) {
                    items.push(React.createElement(
                        'li',
                        { key: i },
                        React.createElement(
                            'a',
                            { href: '#' + item.href },
                            item.text,
                            React.createElement('span', { className: 'fa arrow' })
                        ),
                        React.createElement(
                            'ul',
                            { className: 'nav nav-second-level collapse', 'aria-expanded': 'false',
                                style: { height: '0px' } },
                            self.renderListItems(item.items)
                        )
                    ));
                } else {
                    items.push(React.createElement(
                        'li',
                        { key: i },
                        React.createElement(
                            'a',
                            { href: '#' + item.href },
                            React.createElement('i', { className: item.iconClassName }),
                            ' ',
                            item.text
                        )
                    ));
                }
            });
        }
        return items;
    },
    languageChange: function () {
        var language = $('#langSelect').val();
        if (language != $conn.getLocale()) {
            $conn.setLocale(language);
            window.location.reload();
            $('#langSelect').val(language);
        }
    },
    render: function () {
        var routes = this.renderListItems(this.props.routes);
        if (typeof this.props.child != 'function') {
            this.childEl = this.props.child; //use created object
        } else {
                this.childEl = React.createElement(this.props.child, null); //use object factory
            }
        return React.createElement(
            'div',
            null,
            React.createElement(
                'nav',
                { className: 'navbar navbar-default navbar-static-top', role: 'navigation',
                    style: { marginBottom: '0px' } },
                React.createElement(
                    'div',
                    { className: 'navbar-header' },
                    React.createElement(
                        'a',
                        { className: 'navbar-brand', href: '/' },
                        ' ',
                        lang.t("header")
                    )
                ),
                React.createElement(
                    'ul',
                    { className: 'nav navbar-top-links navbar-right' },
                    React.createElement(
                        'li',
                        { className: 'dropdown' },
                        React.createElement(
                            'div',
                            { className: 'bfh-selectbox' },
                            React.createElement(
                                'select',
                                { className: 'selectpicker', id: 'langSelect', onChange: this.languageChange, value: $conn.getLocale() },
                                React.createElement(
                                    'option',
                                    { value: 'en' },
                                    'En'
                                )
                            )
                        )
                    )
                ),
                React.createElement('div', { id: 'error' }),
                React.createElement(
                    'div',
                    { className: 'navbar-default sidebar', role: 'navigation' },
                    React.createElement(
                        'div',
                        { className: 'sidebar-nav navbar-collapse' },
                        React.createElement(
                            'ul',
                            { className: 'nav', id: 'side-menu' },
                            routes
                        )
                    )
                )
            ),
            React.createElement(
                'div',
                { id: 'page-wrapper' },
                React.createElement(
                    'div',
                    { className: 'container-fluid' },
                    React.createElement(
                        'div',
                        { className: 'row' },
                        React.createElement(
                            'div',
                            { className: 'col-lg-12' },
                            React.createElement('div', { id: 'notification', style: { marginTop: '25px' } }),
                            this.childEl
                        )
                    )
                )
            )
        );
    }
});
