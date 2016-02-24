var UserApp = React.createClass({
    render: function () {
        if (this.props.route == 'publishers/add')
            return (<AddCategory/>);
        else if (this.props.route == 'publishers/show') {
            return(<FilterablePublisherTable/>)
        }
        else return ((<div id="page">
                Page Not Found
            </div>));
    }
});
