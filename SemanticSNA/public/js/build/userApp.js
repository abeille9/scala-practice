var UserApp = React.createClass({
    displayName: 'UserApp',

    render: function () {
        if (this.props.route == 'publishers/add') return React.createElement(AddCategory, null);else if (this.props.route == 'publishers/show') {
            return React.createElement(FilterablePublisherTable, null);
        } else return React.createElement(
            'div',
            { id: 'page' },
            'Page Not Found'
        );
    }
});
