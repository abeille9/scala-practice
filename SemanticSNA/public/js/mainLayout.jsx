var MainLayout = React.createClass({
    componentDidMount: function () {
        $('#side-menu').metisMenu();
    },
    renderListItems: function (menu) {
        var items = [];
        self = this;
        if (menu) {
            $.each(menu, function (i, item) {
                if (item.items) {
                    items.push(
                        <li key={i}>
                            <a href={'#'+item.href}>

                                {item.text}
                                <span className="fa arrow"></span>
                            </a>
                            <ul className="nav nav-second-level collapse" aria-expanded="false"
                                style={{height: '0px'}}>
                                {self.renderListItems(item.items)}
                            </ul>
                        </li>
                    )
                } else {
                    items.push(
                        <li key={i}>
                            <a href={'#'+item.href}><i className={item.iconClassName}></i> {item.text}</a>
                        </li>
                    )
                }
            });
        }
        return items;
    },
    languageChange:function () {
        var language = $('#langSelect').val();
        if(language!=$conn.getLocale()) {
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
            this.childEl = <this.props.child/>; //use object factory
        }
        return (
            <div>
                <nav className="navbar navbar-default navbar-static-top" role="navigation"
                     style={{marginBottom: '0px'}}>
                    <div className="navbar-header">
                        <a className="navbar-brand" href="/"> {lang.t("header")}</a>
                    </div>
                    <ul className="nav navbar-top-links navbar-right">

                        <li className="dropdown">
                        <div className="bfh-selectbox">
                            <select className="selectpicker" id="langSelect" onChange={this.languageChange} value={$conn.getLocale()}>
                                <option value="en">En</option>
                            </select>
                        </div>

                        </li>
                    </ul>
                    <div id="error"></div>
                    <div className="navbar-default sidebar" role="navigation">
                        <div className="sidebar-nav navbar-collapse">
                            <ul className="nav" id="side-menu">
                                {routes}
                            </ul>
                        </div>
                    </div>
                </nav>
                <div id="page-wrapper">
                    <div className="container-fluid">
                        <div className="row">
                            <div className="col-lg-12">

                                <div id="notification" style={{marginTop: '25px'}}>
                                </div>
                                {this.childEl}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
});