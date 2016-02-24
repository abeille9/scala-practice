var PublisherCategoryCell = React.createClass({
    render: function () {
        return (<b>
            {this.props.category}
        </b>);
    }
});

var PublisherText = React.createClass({
    render: function () {
        return ( <i> &nbsp; {this.props.name}</i>);
    }
});

var PublisherTable = React.createClass({
    render: function () {
        var rows = [];
        var groups = this.props.publishers.reduce(function (grouped, item) {
            var key = item.category;
            grouped[key] = grouped[key] || [];
            grouped[key].push(<PublisherText name={item.name} key={item.name}/>);
            return grouped;
        }, {});
        for (var p in groups) {
            if (groups.hasOwnProperty(p)) {
                rows.push(
                    <tr>
                        <td><PublisherCategoryCell category={p} key={p}/></td>
                        <td>{groups[p]}</td>
                    </tr>);
            }
        }
        return (
            <div className="table-responsive">
                <table className="table table-bordered table-hover table-striped">
                    <thead>
                    <tr>
                        <th>{lang.t("publishers.category")}</th>
                        <th>{lang.t("publishers.publishers")}</th>
                    </tr>
                    </thead>
                    <tbody>{rows}</tbody>
                </table>
            </div>
        );
    }
});

var FilterablePublisherTable = React.createClass({
    getInitialState: function () {
        return {
            publishers: []
        };
    },
    componentDidMount: function () {
        self = this;
        publishers = [];
        $conn.get("/publishers/all", function (data) {
            $.each(data.publishers, function (i, item) {
                publishers.push({category: item.category, name: item.name});
            });
            self.setState({publishers: publishers});
        });

    },
    render: function () {
        return (
            <div>
                <PublisherTable publishers={this.state.publishers}/>
            </div>
        );
    }
});

var AddCategory = React.createClass({
    addPublisher: function () {
        var category = $('#cat').val();
        var name = $('#name').val();
        publisher = {
            "category": category,
            "name": name
        };
        if (category && name) {
            $conn.post("/publisher/add", JSON.stringify(publisher), this.responseHandler)
        }
    },
    responseHandler: function (data) {
        $('#name').val("");
    },
    render: function () {
        return (
            <div className="col-lg-6">
                <tbody>
                <tr>
                    <td className="row" align="right">{lang.t("publishers.category")}</td>
                    <td>
                        <div className="form-group">
                            <input className="form-control" id="cat" required/>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td align="right">{lang.t("publishers.screenName")}</td>
                    <td>
                        <div className="form-group">
                            <input type="text" className="form-control" id="name" required/>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td align="right"></td>
                    <td>
                        <div clssName="form-actions">
                            <button type="button" className="btn btn-primary"
                                    onClick={this.addPublisher}> {lang.t("add")}
                            </button>
                        </div>
                    </td>
                </tr>
                </tbody>
            </div>
        );
    }
});