var PublisherCategoryCell = React.createClass({
    displayName: "PublisherCategoryCell",

    render: function () {
        return React.createElement(
            "b",
            null,
            this.props.category
        );
    }
});

var PublisherText = React.createClass({
    displayName: "PublisherText",

    render: function () {
        return React.createElement(
            "i",
            null,
            "   ",
            this.props.name
        );
    }
});

var PublisherTable = React.createClass({
    displayName: "PublisherTable",

    render: function () {
        var rows = [];
        var groups = this.props.publishers.reduce(function (grouped, item) {
            var key = item.category;
            grouped[key] = grouped[key] || [];
            grouped[key].push(React.createElement(PublisherText, { name: item.name, key: item.name }));
            return grouped;
        }, {});
        for (var p in groups) {
            if (groups.hasOwnProperty(p)) {
                rows.push(React.createElement(
                    "tr",
                    null,
                    React.createElement(
                        "td",
                        null,
                        React.createElement(PublisherCategoryCell, { category: p, key: p })
                    ),
                    React.createElement(
                        "td",
                        null,
                        groups[p]
                    )
                ));
            }
        }
        return React.createElement(
            "div",
            { className: "table-responsive" },
            React.createElement(
                "table",
                { className: "table table-bordered table-hover table-striped" },
                React.createElement(
                    "thead",
                    null,
                    React.createElement(
                        "tr",
                        null,
                        React.createElement(
                            "th",
                            null,
                            lang.t("publishers.category")
                        ),
                        React.createElement(
                            "th",
                            null,
                            lang.t("publishers.publishers")
                        )
                    )
                ),
                React.createElement(
                    "tbody",
                    null,
                    rows
                )
            )
        );
    }
});

var FilterablePublisherTable = React.createClass({
    displayName: "FilterablePublisherTable",

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
                publishers.push({ category: item.category, name: item.name });
            });
            self.setState({ publishers: publishers });
        });
    },
    render: function () {
        return React.createElement(
            "div",
            null,
            React.createElement(PublisherTable, { publishers: this.state.publishers })
        );
    }
});

var AddCategory = React.createClass({
    displayName: "AddCategory",

    addPublisher: function () {
        var category = $('#cat').val();
        var name = $('#name').val();
        publisher = {
            "category": category,
            "name": name
        };
        if (category && name) {
            $conn.post("/publisher/add", JSON.stringify(publisher), this.responseHandler);
        }
    },
    responseHandler: function (data) {
        $('#name').val("");
    },
    render: function () {
        return React.createElement(
            "div",
            { className: "col-lg-6" },
            React.createElement(
                "tbody",
                null,
                React.createElement(
                    "tr",
                    null,
                    React.createElement(
                        "td",
                        { className: "row", align: "right" },
                        lang.t("publishers.category")
                    ),
                    React.createElement(
                        "td",
                        null,
                        React.createElement(
                            "div",
                            { className: "form-group" },
                            React.createElement("input", { className: "form-control", id: "cat", required: true })
                        )
                    )
                ),
                React.createElement(
                    "tr",
                    null,
                    React.createElement(
                        "td",
                        { align: "right" },
                        lang.t("publishers.screenName")
                    ),
                    React.createElement(
                        "td",
                        null,
                        React.createElement(
                            "div",
                            { className: "form-group" },
                            React.createElement("input", { type: "text", className: "form-control", id: "name", required: true })
                        )
                    )
                ),
                React.createElement(
                    "tr",
                    null,
                    React.createElement("td", { align: "right" }),
                    React.createElement(
                        "td",
                        null,
                        React.createElement(
                            "div",
                            { clssName: "form-actions" },
                            React.createElement(
                                "button",
                                { type: "button", className: "btn btn-primary",
                                    onClick: this.addPublisher },
                                " ",
                                lang.t("add")
                            )
                        )
                    )
                )
            )
        );
    }
});
