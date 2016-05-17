var UserApp = React.createClass({
    displayName: 'UserApp',

    render: function () {
        if (this.props.route == 'publishers/add') return React.createElement(AddPublisher, null);else if (this.props.route == 'publishers/show') {
            return React.createElement(FilterablePublisherTable, null);
        } else if (this.props.route == 'publishers/remove') {
            return React.createElement(RemovePublisher, null);
        } else if (this.props.route == 'concepts') {
            return React.createElement(Words, null);
        } else if (this.props.route == 'analysis') {
            return React.createElement(
                'div',
                { id: 'page' },
                'Welcome!'
            );
        } else return React.createElement(
            'div',
            { id: 'page' },
            'Page Not Found'
        );
    }
});
